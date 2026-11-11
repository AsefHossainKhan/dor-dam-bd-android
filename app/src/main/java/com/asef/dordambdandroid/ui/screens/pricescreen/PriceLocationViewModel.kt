package com.asef.dordambdandroid.ui.screens.pricescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asef.dordambdandroid.data.remote.models.places.CreatePlace
import com.asef.dordambdandroid.data.remote.models.places.PlaceView
import com.asef.dordambdandroid.data.remote.models.prices.location.PriceLocationFields
import com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid.PriceByItemIdItem
import com.asef.dordambdandroid.location.ChosenLocation
import com.asef.dordambdandroid.location.DeviceLocation
import com.asef.dordambdandroid.location.GeoPoint
import com.asef.dordambdandroid.location.LocationSource
import com.asef.dordambdandroid.location.StickyLocationStore
import com.asef.dordambdandroid.location.buildSuggestions
import com.asef.dordambdandroid.repository.DorDamBDRepository
import com.asef.dordambdandroid.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which price sheet a location action is for. */
enum class LocationTarget { ADD, EDIT }

/** The type-a-place search (shared by both sheets; only one is open at a time). */
data class PlaceSearchState(
    val target: LocationTarget? = null,
    val query: String = "",
    val results: List<PlaceView> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

/** The pick-on-map dialog. */
data class MapPickerState(
    val target: LocationTarget? = null,
    val initial: GeoPoint? = null,
    val resolving: Boolean = false,
    val error: String? = null,
)

/**
 * The location chip of the add-price and edit-price sheets.
 *
 * SPEC: spec-5f5a30 (add sheet: suggestions, trimming, sticky location),
 * spec-14240b (type a place, pick on map), spec-628f81 (edit sheet: no GPS).
 */
@HiltViewModel
class PriceLocationViewModel @Inject constructor(
    private val repository: DorDamBDRepository,
    private val deviceLocation: DeviceLocation,
    private val stickyStore: StickyLocationStore,
) : ViewModel() {

    // Add sheet
    private val _addLocation = MutableStateFlow<ChosenLocation?>(null)
    val addLocation = _addLocation.asStateFlow()
    private val _suggestions = MutableStateFlow<List<PlaceView>>(emptyList())
    val suggestions = _suggestions.asStateFlow()
    private val _loadingSuggestions = MutableStateFlow(false)
    val loadingSuggestions = _loadingSuggestions.asStateFlow()
    private var addFix: GeoPoint? = null
    private var addSheetJob: Job? = null

    // Edit sheet
    private val _editLocation = MutableStateFlow<ChosenLocation?>(null)
    val editLocation = _editLocation.asStateFlow()
    private var editDirty = false

    // Dialogs
    private val _search = MutableStateFlow(PlaceSearchState())
    val search = _search.asStateFlow()
    private var searchJob: Job? = null
    private val _mapPicker = MutableStateFlow(MapPickerState())
    val mapPicker = _mapPicker.asStateFlow()

    /** Asked once, on the first add-price sheet; later sheets never ask again. */
    fun shouldAskPermission(): Boolean =
        !deviceLocation.hasAnyPermission() && !stickyStore.permissionAsked

    fun markPermissionAsked() {
        stickyStore.permissionAsked = true
    }

    /**
     * The add sheet opened (after any permission prompt): pre-fill the sticky
     * location if still fresh, then fetch suggestions for the device fix.
     * Without permission or a fix, suggestions are skipped silently.
     */
    fun onAddSheetOpened(nowMs: Long = System.currentTimeMillis()) {
        addSheetJob?.cancel()
        addFix = null
        _suggestions.value = emptyList()
        val sticky = stickyStore.load()?.takeIf { it.isValid(nowMs, currentFix = null) }
        _addLocation.value = sticky?.carriedOver()

        if (!deviceLocation.hasAnyPermission()) return
        addSheetJob = viewModelScope.launch {
            _loadingSuggestions.value = true
            try {
                val fix = deviceLocation.currentFix() ?: return@launch
                addFix = fix
                // Moved more than about 1 km since: the carried-over location expires.
                if (sticky != null && !sticky.isValid(nowMs, fix) &&
                    _addLocation.value?.source == LocationSource.AUTO
                ) {
                    _addLocation.value = null
                }
                val nearby = repository.getNearbyPlaces(fix.lat, fix.lng) ?: return@launch
                _suggestions.value = buildSuggestions(nearby.osm, nearby.places)
            } finally {
                _loadingSuggestions.value = false
            }
        }
    }

    fun onAddSheetClosed() {
        addSheetJob?.cancel()
    }

    fun selectSuggestion(place: PlaceView) {
        _addLocation.value = ChosenLocation.suggested(place, addFix)
    }

    /** The location to send with a new price; null saves it without one. */
    fun addFields(): PriceLocationFields? = _addLocation.value?.toFields()

    /** Remember the saved price's location for the next entry, or forget it. */
    fun onPriceAdded(nowMs: Long = System.currentTimeMillis()) {
        val location = _addLocation.value
        if (location == null) stickyStore.clear()
        else stickyStore.save(location, nowMs, addFix)
    }

    /** The edit sheet opened for [price]: its current location, and no GPS at all. */
    fun onEditSheetOpened(price: PriceByItemIdItem) {
        _editLocation.value = ChosenLocation.ofExistingPrice(price.place, price.locationSource)
        editDirty = false
    }

    /**
     * The location to send with an edit: null when untouched, so the price keeps
     * its stored location (coordinates included); [PriceLocationFields.NONE] when removed.
     */
    fun editFields(): PriceLocationFields? =
        if (!editDirty) null else _editLocation.value?.toFields() ?: PriceLocationFields.NONE

    fun trim(target: LocationTarget, index: Int) {
        val current = locationOf(target) ?: return
        setLocation(target, current.trimTo(index))
    }

    fun clear(target: LocationTarget) = setLocation(target, null)

    // Type a place

    fun openSearch(target: LocationTarget) {
        searchJob?.cancel()
        _search.value = PlaceSearchState(target = target)
    }

    fun closeSearch() {
        searchJob?.cancel()
        _search.value = PlaceSearchState()
    }

    fun onSearchQuery(query: String) {
        _search.value = _search.value.copy(query = query, error = null)
        searchJob?.cancel()
        if (query.isBlank()) {
            _search.value = _search.value.copy(results = emptyList(), loading = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _search.value = _search.value.copy(loading = true)
            when (val result = repository.searchPlaces(query.trim())) {
                is Resource.Success -> _search.value =
                    _search.value.copy(results = result.data.orEmpty(), loading = false)
                else -> _search.value =
                    _search.value.copy(loading = false, error = result.errorMessage)
            }
        }
    }

    fun pickSearchResult(place: PlaceView) {
        val target = _search.value.target ?: return
        setLocation(target, ChosenLocation.typed(place))
        closeSearch()
    }

    /** No shared place matched: create one with the typed name, and select it. */
    fun createTypedPlace(name: String) {
        val target = _search.value.target ?: return
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _search.value = _search.value.copy(loading = true, error = null)
            when (val result = repository.createPlace(CreatePlace(name = trimmed))) {
                is Resource.Success -> {
                    setLocation(target, ChosenLocation.typed(result.data!!))
                    closeSearch()
                }
                else -> _search.value =
                    _search.value.copy(loading = false, error = result.errorMessage)
            }
        }
    }

    // Pick on map

    /** The add sheet starts the map at the device fix; the edit sheet never uses GPS. */
    fun openMap(target: LocationTarget) {
        val current = locationOf(target)
        val initial = current?.takeIf { it.hasCoordinates }?.let { GeoPoint(it.lat!!, it.lng!!) }
            ?: if (target == LocationTarget.ADD) addFix else null
        _mapPicker.value = MapPickerState(target = target, initial = initial)
    }

    fun closeMap() {
        _mapPicker.value = MapPickerState()
    }

    /**
     * A pin was dropped. With a name, the pin becomes a shared place there (de-duplicated
     * by the backend); without one, it is described by the OSM path at the pin.
     * Either way the price keeps the pin's exact coordinates.
     */
    fun confirmPin(pin: GeoPoint, name: String?) {
        val target = _mapPicker.value.target ?: return
        viewModelScope.launch {
            _mapPicker.value = _mapPicker.value.copy(resolving = true, error = null)
            val osm = repository.getNearbyPlaces(pin.lat, pin.lng)?.osm
            val place: PlaceView? = if (!name.isNullOrBlank()) {
                val parentId = osm?.pathOrSelf?.firstOrNull { it.level != "market" }?.id
                when (val created = repository.createPlace(
                    CreatePlace(name = name.trim(), level = "market", parentId = parentId, lat = pin.lat, lng = pin.lng)
                )) {
                    is Resource.Success -> created.data
                    else -> {
                        _mapPicker.value = _mapPicker.value.copy(resolving = false, error = created.errorMessage)
                        return@launch
                    }
                }
            } else {
                osm
            }
            setLocation(target, ChosenLocation.pinned(place, pin))
            closeMap()
        }
    }

    private fun locationOf(target: LocationTarget) = when (target) {
        LocationTarget.ADD -> _addLocation.value
        LocationTarget.EDIT -> _editLocation.value
    }

    private fun setLocation(target: LocationTarget, location: ChosenLocation?) {
        when (target) {
            LocationTarget.ADD -> _addLocation.value = location
            LocationTarget.EDIT -> {
                _editLocation.value = location
                editDirty = true
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
