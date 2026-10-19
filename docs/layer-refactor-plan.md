# Layer refactor plan

Goal: bring the app in line with `AGENTS.md` / `.cursorrules` one slice at a time, without a big-bang rewrite and without wiping local data.

Ground rules for every phase:
- **No Room schema change** unless a phase says so. `ShuttleDatabase` uses `fallbackToDestructiveMigration`, so a version bump deletes unsynced passes on drivers' phones.
- Each phase ends with a green `./gradlew assembleDebug testDebugUnitTest` and is shippable on its own (one commit or PR per phase).
- Only touch the files the phase lists. Old patterns in other screens stay until their own phase.

---

## Phase 1: `ShuttlePassRepository` speaks domain models

**Why first:** every screen reaches the data layer through this interface (`domain/repository/ShuttlePassRepository.kt:3-7` imports entities, DTOs and `retrofit2.Response`). Fixing it removes most of the `presentation → data` imports, because the screens get those types from here.

### 1.1 Reshape the domain models (`domain/model`)
Nothing outside `data/mappers` uses `ShuttlePass`, `ShuttlePassWithPassenger` or `PassengerQR` today, so they can be reshaped safely. Make them immutable and mirror the entity field names. That keeps the presentation-side change a mechanical type rename:

```kotlin
data class ShuttlePass(
    val id: String = UUID.randomUUID().toString(),
    val routeId: String = "", val provider: String = "", val driver: String = "",
    val plateNumber: String = "", val date: String = "", val dateCreated: Long = 0L,
    val tripType: String = "", val departure: String = "", val arrival: String = "",
    val isSynced: Boolean = false, val isLateShuttle: Boolean = false, val isDraft: Boolean = true,
)
data class PassengerQR(/* same fields as PassengerQREntity, all val */)
data class ShuttlePassWithPassengers(val shuttlePass: ShuttlePass, val passengers: List<PassengerQR>)
data class SyncResult(val syncedCount: Int, val message: String?)
```

### 1.2 Mappers (`data/mappers`)
- `ShuttlePassEntity.toDomain()` / `ShuttlePass.toEntity()`
- `PassengerQREntity.toDomain()` / `PassengerQR.toEntity()`
- `ShuttlePassWithPassengerEntity.toDomain()`
- Keep `ShuttlePassWithPassengerEntity.toDto()` (used only inside the repo impl from now on). Delete the domain `toDto()` overloads and the commented-out stubs.

### 1.3 New interface signatures
```kotlin
interface ShuttlePassRepository {
    fun observePasses(driverId: String, filter: PassFilter): Flow<List<ShuttlePassWithPassengers>>   // replaces 3 getAll…Filtered… methods
    fun observePass(id: String): Flow<ShuttlePassWithPassengers?>
    fun observeDraft(driverId: String): Flow<ShuttlePass?>
    fun observeUnsyncedCount(driverId: String): Flow<Int>
    fun observeLateCount(driverId: String): Flow<Int>
    suspend fun getDraftId(driverId: String): String?
    suspend fun insert(pass: ShuttlePass): Resource<Unit>
    suspend fun update(passes: List<ShuttlePass>): Resource<Unit>
    suspend fun delete(id: String): Resource<Unit>
    suspend fun finalizeDraft(driverId: String, pass: ShuttlePass, passengers: List<PassengerQR>): Resource<Unit>
    suspend fun syncUnsynced(driverId: String): Resource<SyncResult>
    suspend fun syncPass(id: String): Resource<SyncResult>
    suspend fun purgeExpired(driverId: String): Resource<Int>      // deleteShuttlePassOnDue + deleteAllDraftedShuttlePass
}
enum class PassFilter { All, Late, NotSynced }   // domain/model; presentation's FilterType goes away
```

### 1.4 `ShuttlePassRepositoryImpl`
- Map at the boundary. Wrap writes in `try/catch → Resource.Error`, with no swallowed exceptions.
- `finalizeDraft`: run both updates inside `database.withTransaction {}`, and return `Error("No active shuttle pass")` when there's no draft. *(Fixes review finding 5.)*
- `syncUnsynced` / `syncPass`: load rows → `toDto()` → POST → on 2xx, `dao.markSynced(postedIds)`. Mark **only the ids that were posted**, so a pass finalized during the request stays unsynced. Map SocketTimeout/Connect/HttpException to messages (reuse the existing strings). *(Fixes review finding 1 at the root.)*
- Network work runs in `withContext(Dispatchers.IO)`, per AGENTS.md.

### 1.5 DAO (`ShuttlePassDao`), queries only, no version bump
- `@Query("UPDATE shuttle_pass SET isSynced = 1 WHERE id IN (:ids)") suspend fun markSynced(ids: List<String>)`
- `countLateShuttle`: add `AND isDraft = 0`. *(Fixes finding 7.)*
- Remove now-unused duplicates (`getShuttlePassWithPassengersById`, `getDraftedShuttlePassWithPassengers`).

### 1.6 Callers
- `ShuttleViewModel`, `QrAnalyzerViewModel`, `FilterViewModel`: switch to the new methods. Change `ShuttlePassEntity` → `ShuttlePass` and `PassengerQREntity` → `PassengerQR` in their public types.
- The screens that import those entity types (`EditShuttlePassInformationScreen`, `AVCurrentPassScreenTest`, `QRScannerScreen`, `HomeScreen`, `presentation/state/*`) just change the import to the domain type. No structural UI change in this phase.
- `ValidateQRCodeInput` takes `List<PassengerQR>`.

**Done when:** `grep -rn "import com.example.shuttleapp.data" domain/repository/ShuttlePassRepository.kt presentation` drops by every `ShuttlePass*`/`PassengerQR*` entity/DTO import, and the app builds and behaves the same (draft → finalize → sync → purge).

---

## Phase 2: Sync screen (`ShuttlePassListScreen`) to MVI

Mockups: `08-shuttle-pass-list.png`, `12-sync-success.png`, `13-sync-error.png`. Use the `shuttle-add-screen` and `android-presentation-mvi` skills.

### 2.1 Files (new, in `presentation/passlist/`)
```kotlin
data class ShuttlePassListState(
    val filter: PassFilter = PassFilter.All,
    val items: List<PassListItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val sync: SyncStatus = SyncStatus.Idle,
)
data class PassListItemUi(val id: String, val title: String, val dateLabel: String,
                          val passengerCount: Int, val isSynced: Boolean, val isLate: Boolean)  // formatting done in VM
sealed interface SyncStatus { data object Idle; data object Running; data class Success(val message: String); data class Failed(val message: String) }

sealed interface ShuttlePassListAction {
    data class SelectFilter(val filter: PassFilter); data object UploadAll; data object DismissSyncResult
    data class OpenPass(val id: String); data object Back
}
```
- `ShuttlePassListViewModel(savedStateHandle, repo)`: read `driverId` from `savedStateHandle["id"]`. Hold `filter` in a `MutableStateFlow` and use `filter.flatMapLatest { repo.observePasses(driverId, it) }` → `stateIn`. This also fixes the current `FilterViewModel` bug, where every filter tap starts another collector that is never cancelled. `UploadAll` calls `repo.syncUnsynced(driverId)` and updates `sync`. When there's nothing to upload, show `Failed("No unsynced shuttle pass")`, or emit an event for a snackbar.
- `ShuttlePassListRoot(onBack, onOpenPass)`: does `hiltViewModel()` + `collectAsStateWithLifecycle()`, and routes `OpenPass`/`Back` to the lambdas.
- `ShuttlePassListScreen(state, onAction)`: dumb and previewable, split into components under 100 lines each (filter bar, list item, sync dialogs).

### 2.2 Wiring and cleanup
- `Navigation.kt`: this route renders `ShuttlePassListRoot`, and the lambdas call `navController`. Keep the string route for now; type-safe routes come in a later phase.
- Delete `FilterViewModel`, `FilterState`, `UploadUiState`, and the old screen body.
- `ShuttlePassItemScreen` also uploads (lines 79-120). For this phase, replace its post + screen-side `updateShuttlePass` with `ShuttleViewModel.syncPass(id)` → `repo.syncPass(id)`. Its full MVI conversion comes in Phase 4.

### 2.3 Tests (first real tests in the repo)
- Add `kotlinx-coroutines-test`, `app.cash.turbine`, and `androidx.room:room-testing` to the version catalog.
- `ShuttlePassListViewModelTest` with a `FakeShuttlePassRepository`: switching filters, upload success/failure states, and "nothing to upload".
- `ShuttlePassDaoTest` (instrumented, in-memory DB): `markSynced` only touches the given ids, and `countLateShuttle` ignores drafts.

**Done when:** the sync screen has no `data` imports, takes `state` + `onAction` only, Upload All no longer crashes, and the tests pass.

---

## Phase 3: Network config (logging, Retrofit, base URL, auth)

### 3.1 Logging and Retrofit (`di/AppModule.kt`)
- `buildFeatures { buildConfig = true }` in `app/build.gradle.kts`.
- `@Provides @Singleton` for `HttpLoggingInterceptor` (`if (BuildConfig.DEBUG) BODY else NONE`), `OkHttpClient`, and **one** `Retrofit`. `AuthApi` and `ShuttleApi` are created from it. Remove the class fields.
- `API.BASE_URL` → `buildConfigField("String", "BASE_URL", …)` per build type: `http://10.0.2.2:5105/api/` for debug, an `https://` host for release.

### 3.2 Bearer token: **blocked on the backend**
`LoginResponseBodyDto` is `{ message, userData }`, with no token. Order of work:
1. Backend: `users/login` returns a JWT (e.g. `token`, `expiresAt`), and the API validates `[Authorize]` on `shuttlepasses/` etc.
2. App: `data/local/TokenStore` backed by `EncryptedSharedPreferences` (add `androidx.security:security-crypto`). `AuthRepositoryImpl.postLogin` saves the token, and `LogoutUseCase` clears it.
3. `AuthInterceptor(tokenStore)` adds `Authorization: Bearer …` to every request except `users/login` and `users/register`. A 401 clears the token and surfaces a "session expired" error through `Resource.Error`.

Steps 2–3 can be built before 1 lands. The interceptor is a no-op while no token is stored.

---

## Phase 4+: remaining screens, one per PR

Order, chosen by how much each depends on what's already done:

| # | Screen | Notes |
|---|---|---|
| 4 | `ShuttlePassItemScreen` | Small; already uses `observePass` + `syncPass` from Phase 1–2. |
| 5 | `EditShuttlePassInformationScreen` | Needs route/shuttle lookups. Make `RoutesRepository`/`ShuttleRepository` domain-only here, and fix the `coil.network.HttpException` import and catch order (finding 6). |
| 6 | `HomeScreen` (dashboard) | Counts + purge on open. Fix `isLateShuttle` here by moving it to `domain/usecase/IsLateShuttleUseCase(clock)` using `HOUR_OF_DAY` (finding 2). |
| 7 | `AVCurrentPassScreenTest` + `QRScannerScreen` | Active pass / manifest / scanner. Make `PassengerQRRepository` domain-only, and make `insertPassengerQR(driverId, …)` return `Resource` (finding 4). This retires the rest of `ShuttleViewModel`. |
| 8 | `LoginScreen` / `RegisterScreen` | `AuthRepository` takes `LoginCredentials` / `Registration` domain models. Handle non-2xx in `postRegister` (finding 3), and use `val`-only state with no in-place mutation. |
| 9 | Navigation | Once every screen has a Root, move to `@Serializable` routes (`android-navigation` skill). |

When phase 7 lands, `ShuttleViewModel` should be gone. When phase 8 lands, `grep -rn "import com.example.shuttleapp.data" app/src/main/java/com/example/shuttleapp/{domain,presentation}` should return nothing, and the "Known gaps" section of `CLAUDE.md` can be deleted.
