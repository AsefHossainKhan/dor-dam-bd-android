package com.asef.dordambdandroid.location

import com.asef.dordambdandroid.data.remote.models.places.PlacePathStep
import com.asef.dordambdandroid.data.remote.models.places.PlaceView
import com.asef.dordambdandroid.data.remote.models.prices.location.PriceLocationFields
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// SPEC: spec-5f5a30, spec-14240b, spec-628f81
class ChosenLocationTest {

    private val karwan = PlacePathStep(3, "Karwan Bazar", "market")
    private val tejgaon = PlacePathStep(2, "Tejgaon", "area")
    private val dhaka = PlacePathStep(1, "Dhaka", "city")
    private val osmPlace = PlaceView(
        id = 3, name = "Karwan Bazar", level = "market",
        path = listOf(karwan, tejgaon, dhaka), label = "Karwan Bazar › Tejgaon › Dhaka"
    )
    private val fix = GeoPoint(23.7510, 90.3935)

    @Test
    fun suggestion_keepsTheDeviceFix_whileTheFinestLevelIsKept() {
        val chosen = ChosenLocation.suggested(osmPlace, fix)
        assertEquals("Karwan Bazar › Tejgaon › Dhaka", chosen.label)
        assertEquals(PriceLocationFields(3, fix.lat, fix.lng, "suggested"), chosen.toFields())
    }

    @Test
    fun trimmingToTejgaon_sendsOnlyTejgaon_andNoCoordinates() {
        val trimmed = ChosenLocation.suggested(osmPlace, fix).trimTo(1)
        assertEquals("Tejgaon › Dhaka", trimmed.label)
        assertEquals(PriceLocationFields(2, null, null, "suggested"), trimmed.toFields())
    }

    @Test
    fun trimmingToTheFinestLevelOrOutOfRange_changesNothing() {
        val chosen = ChosenLocation.suggested(osmPlace, fix)
        assertEquals(chosen, chosen.trimTo(0))
        assertEquals(chosen, chosen.trimTo(3))
    }

    @Test
    fun suggestionWithoutAFix_hasNoCoordinates() {
        val fields = ChosenLocation.suggested(osmPlace, null).toFields()
        assertNull(fields.lat)
        assertNull(fields.lng)
    }

    @Test
    fun typedPlace_isTyped_withoutCoordinates() {
        val shared = PlaceView(id = 9, name = "Rajarbag", level = "market", lat = 23.74, lng = 90.42)
        assertEquals(PriceLocationFields(9, null, null, "typed"), ChosenLocation.typed(shared).toFields())
    }

    @Test
    fun pin_keepsExactCoordinates_andIsPinned() {
        val pin = GeoPoint(23.7412345, 90.4198765)
        assertEquals(
            PriceLocationFields(3, pin.lat, pin.lng, "pinned"),
            ChosenLocation.pinned(osmPlace, pin).toFields()
        )
        assertEquals(
            PriceLocationFields(null, pin.lat, pin.lng, "pinned"),
            ChosenLocation.pinned(null, pin).toFields()
        )
    }

    @Test
    fun existingPrice_withoutLocation_hasNone() {
        assertNull(ChosenLocation.ofExistingPrice(null, null))
    }

    @Test
    fun existingPrice_withPlace_keepsPathAndSource() {
        val existing = ChosenLocation.ofExistingPrice(osmPlace, "pinned")!!
        assertEquals(listOf(karwan, tejgaon, dhaka), existing.path)
        assertEquals(LocationSource.PINNED, existing.source)
        assertFalse(existing.hasCoordinates)
    }

    @Test
    fun shortLabel_isThePlaceAndItsCity() {
        assertEquals("Karwan Bazar, Dhaka", shortPlaceLabel(listOf(karwan, tejgaon, dhaka)))
        assertEquals("Tejgaon, Dhaka", shortPlaceLabel(listOf(tejgaon, dhaka)))
        assertEquals("Dhaka", shortPlaceLabel(listOf(dhaka)))
        assertEquals("", shortPlaceLabel(emptyList()))
    }

    @Test
    fun shortLabel_withoutACity_usesTheCoarsestLevel() {
        val division = PlacePathStep(5, "Dhaka Division", "division")
        assertEquals("Karwan Bazar, Dhaka Division", shortPlaceLabel(listOf(karwan, tejgaon, division)))
    }

    @Test
    fun suggestions_areTheOsmPathThenNearestShared_atMostThree() {
        val shared = (10..14).map { PlaceView(id = it, name = "P$it", level = "market") }
        val result = buildSuggestions(osmPlace, shared)
        assertEquals(listOf(3, 10, 11), result.map { it.id })
    }

    @Test
    fun suggestions_withoutOsm_areSharedOnly_withoutDuplicates() {
        val a = PlaceView(id = 1, name = "A")
        assertEquals(listOf(1), buildSuggestions(null, listOf(a, a)).map { it.id })
        assertTrue(buildSuggestions(null, emptyList()).isEmpty())
    }
}
