# Dor Dam BD Android — Copilot Instructions

> Crowd-sourced commodity price tracking app for Bangladesh. Two-screen MVVM app with Jetpack Compose.

---

## Project Identity

| Property | Value |
|---|---|
| Package | `com.asef.dordambdandroid` |
| Min SDK | 26 · Target/Compile SDK: 35 · Version: 1.0.5 |
| Language | Kotlin · JVM target 17 |
| Architecture | MVVM + Clean Architecture + Hilt DI |

---

## Package Structure

```
app/src/main/java/com/asef/dordambdandroid/
├── MainActivity.kt                    ← Entry point (@AndroidEntryPoint)
├── DorDamBDApplication.kt             ← @HiltAndroidApp, Timber.DebugTree
├── Navigation.kt                      ← NavHost with 2 routes
├── di/
│   └── AppModule.kt                   ← @Module @InstallIn(SingletonComponent)
├── repository/
│   └── DorDamBDRepository.kt          ← Business logic, returns Flow<Resource<T>>
├── data/remote/
│   ├── DorDamBDAPI.kt                 ← Retrofit interface (all suspend funs)
│   └── models/
│       ├── items/
│       │   ├── summary/ItemSummary.kt
│       │   ├── getitems/GetItems.kt + GetItemsItem.kt
│       │   ├── createitem/CreateItem.kt + CreateItemResponse.kt
│       │   └── edititem/EditItem.kt + EditItemResponse.kt
│       └── prices/
│           ├── pricebyitemid/PriceByItemId.kt + PriceByItemIdItem.kt
│           ├── addpricebyitemid/AddPriceByItemId.kt + Item.kt
│           ├── addpriceresponse/AddPriceResponse.kt + Item.kt
│           └── editprice/EditPrice.kt + EditPriceResponse.kt
├── ui/
│   ├── screens/
│   │   ├── Screen.kt                  ← sealed class Screen (route definitions)
│   │   ├── homescreen/
│   │   │   ├── HomeScreen.kt          ← Items list, search, add/edit item, quick-add price
│   │   │   └── HomeViewModel.kt       ← @HiltViewModel
│   │   └── pricescreen/
│   │       ├── PriceScreen.kt         ← Price list, add/edit price
│   │       └── PriceViewModel.kt      ← @HiltViewModel
│   ├── components/
│   │   ├── PullToRefreshLazyColumn.kt ← Generic <T> pull-to-refresh list
│   │   ├── AddFAB.kt                  ← Reusable FAB + ModalBottomSheet
│   │   └── EditBottomSheet.kt         ← Reusable edit bottom sheet
│   └── theme/
│       ├── Color.kt · Type.kt · Theme.kt
└── util/
    ├── Resource.kt                    ← sealed class Resource<T> (Loading/Success/Error)
    └── Configuration.kt              ← BASE_URL constant
```

---

## Navigation

Two routes defined in `Navigation.kt` via `NavHost`:

| Screen | Route | Args |
|---|---|---|
| `HomeScreen` | `home_screen` | none |
| `PriceScreen` | `price_screen/{itemId}/{itemName}` | itemId: Int, itemName: String (URL-encoded) |

- Args are URL-encoded with `URLEncoder.encode()` when navigating and decoded with `URLDecoder.decode()` when received.
- Route constants live in `sealed class Screen` in `Screen.kt`.
- `withArgs(vararg args: String)` encodes and appends args to the route string.

---

## API

**Base URL (dev):** `http://192.168.66.132:3000/`  
**Base URL (prod):** `https://dordambd.duckdns.org/` (commented out in `Configuration.kt` and `AppModule.kt`)

| Method | Endpoint | Request | Response |
|---|---|---|---|
| GET | `/items` | — | `GetItems` |
| GET | `/items/summary` | — | `List<ItemSummary>` |
| POST | `/items` | `CreateItem` | `CreateItemResponse` |
| PATCH | `/items/{id}` | `EditItem` | `EditItemResponse` |
| GET | `/prices/items/{id}` | — | `PriceByItemId` |
| POST | `/prices` | `AddPriceByItemId` | `AddPriceResponse` |
| PATCH | `/prices/{id}` | `EditPrice` | `EditPriceResponse` |

All Retrofit methods are `suspend` functions. All models use `@SerializedName` Gson annotations.

---

## Repository Layer

**File:** `DorDamBDRepository.kt` · `@ActivityScoped`

All public methods return `Flow<Resource<T>>`:
1. Emits `Resource.Loading()`
2. Calls API inside `withTimeout(10_000)` (10-second hard timeout)
3. Emits `Resource.Success(data)` or `Resource.Error(message)`

**Methods:**
- `getItems()` → `Flow<Resource<GetItems>>`
- `getItemsSummary()` → `Flow<Resource<List<ItemSummary>>>`
- `createItem(name)` → `Flow<Resource<CreateItemResponse>>`
- `editItem(id, name)` → `Flow<Resource<EditItemResponse>>`
- `getPricesByItemId(id)` → `Flow<Resource<PriceByItemId>>`
- `addPriceByItemId(itemId, price)` → `Flow<Resource<AddPriceResponse>>`
- `editPrice(id, price)` → `Flow<Resource<EditPriceResponse>>`
- `getLatestPricesForItem(id)` → direct (no Flow), used for price trend calculation

---

## Resource Sealed Class

```kotlin
// util/Resource.kt
sealed class Resource<T>(val data: T? = null, val errorMessage: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(data: T? = null, errorMessage: String) : Resource<T>(data, errorMessage)
    class Loading<T> : Resource<T>()
}
```

Collect in ViewModel with:
```kotlin
repo.someMethod().collect { result ->
    when (result) {
        is Resource.Loading -> { /* set isLoading = true */ }
        is Resource.Success -> { /* use result.data */ }
        is Resource.Error   -> { /* use result.errorMessage */ }
    }
}
```

---

## HomeScreen & HomeViewModel

**HomeScreen features:**
- `PullToRefreshLazyColumn` showing all items
- Search box at top → fuzzy search via `FuzzySearch.ratio()` (top 5 results shown)
- Item card: name + relative timestamp + trend icon (↑↓→) + price badge
- Single-tap card → navigate to `PriceScreen`
- Long-tap card → `EditItemBottomSheet`
- Tap price badge → `QuickAddPriceFromHome` bottom sheet
- FAB → `AddItemFab` (create new item)

**HomeViewModel key state:**
```kotlin
val itemList: StateFlow<GetItems>
val itemPriceInfo: StateFlow<Map<Int, ItemPriceInfo>>   // latestPrice + PriceTrend per itemId
val searchText: StateFlow<String>
val isLoading: StateFlow<Boolean>
val hasError: StateFlow<Boolean>
// Bottom sheet visibility flags, edit state, quick-add state...
```

**`ItemPriceInfo`** data class: `latestPrice: Float?`, `trend: PriceTrend`  
**`PriceTrend`** enum: `UP`, `DOWN`, `STABLE`, `UNKNOWN`

**Utility functions in HomeScreen.kt:**
- `relativeTime(isoDate: String)` → "5 minutes ago", "2 days ago", etc.
- `formatPrice(price: Float)` → "100" (drops `.0`) or "100.5"

---

## PriceScreen & PriceViewModel

**PriceScreen features:**
- `PullToRefreshLazyColumn` showing all prices for selected item
- Header shows item name + column labels ("Item Price", "Upload Date")
- Price row: price in BDT + date formatted as `dd/mm/yyyy hh:mm AM/PM` (ISO UTC → local time)
- Long-tap price row → `EditPriceBottomSheet`
- FAB → `AddPriceFab`

**PriceViewModel key state:**
```kotlin
val priceList: StateFlow<PriceByItemId>
val itemId: StateFlow<Int>
val isLoading: StateFlow<Boolean>
val hasError: StateFlow<Boolean>
// editBottomSheetVisibility, price, priceId, priceTextError...
```

> Note: `PriceViewModel.editItem(id, price)` edits a *price*, not an item — the name is misleading.

---

## Reusable UI Components

### `PullToRefreshLazyColumn<T>`
Generic pull-to-refresh list. Key params:
- `items: List<T>` — data
- `isRefreshing: Boolean` — current refresh state
- `onRefresh: () -> Unit` — called on pull
- `extraContent: @Composable () -> Unit` — header slot (search box, item name)
- `itemContent: @Composable (T) -> Unit` — row composable

### `AddFAB`
FAB that opens a `ModalBottomSheet`. Pass content as a composable lambda.

### `EditBottomSheet`
`ModalBottomSheet` for edit operations. Used for both Edit Item and Edit Price.

---

## Dependency Injection (Hilt)

`AppModule.kt` is `@InstallIn(SingletonComponent::class)` and provides:
- `DorDamBDAPI` — Retrofit singleton with OkHttpClient (HttpLoggingInterceptor at BODY level)
- `DorDamBDRepository` — singleton wrapping the API
- `SharedPreferences` — provided but currently unused

All ViewModels use `@HiltViewModel` + `@Inject constructor(private val repo: DorDamBDRepository)`.  
All Activities/Fragments use `@AndroidEntryPoint`.

---

## Key Dependencies

| Library | Version | Purpose |
|---|---|---|
| Compose BOM | 2024.02.01 | Jetpack Compose UI |
| Retrofit | 2.9.0 | HTTP client |
| Gson Converter | 2.9.0 | JSON serialization |
| Hilt | 2.44.2 | Dependency injection |
| Coroutines | 1.6.4 | Async/reactive |
| Navigation Compose | 2.7.0-alpha01 | Screen navigation |
| Coil Compose | 2.2.2 | Image loading |
| FuzzyWuzzy | 1.2.0 | Fuzzy string search |
| Timber | 5.0.1 | Logging |

---

## Common Task Recipes

### Add a new API endpoint
1. Add suspend fun to `DorDamBDAPI.kt`
2. Add a Flow-returning method to `DorDamBDRepository.kt` (use `withTimeout(10_000)` pattern)
3. Call from ViewModel using `viewModelScope.launch { repo.newMethod().collect { … } }`

### Add a new screen
1. Add route object to `sealed class Screen` in `Screen.kt`
2. Add composable destination in `Navigation.kt` NavHost
3. Create `NewScreen.kt` + `NewViewModel.kt` in `ui/screens/newscreen/`
4. Annotate ViewModel with `@HiltViewModel`

### Add a new data model
1. Create package under `data/remote/models/`
2. Add data class with `@SerializedName` on each field
3. Reference in `DorDamBDAPI.kt` return type

### Show a bottom sheet for editing
- Use the existing `EditBottomSheet` composable (pass visibility flag + content lambda)
- Drive visibility from a `StateFlow<Boolean>` in the ViewModel

---

## Manifest Notes

- **INTERNET** permission required (already in manifest)
- `usesCleartextTraffic="true"` — allows HTTP to dev server; remove for prod builds
- `DorDamBDApplication` is the app class
- `MainActivity` is the single activity
