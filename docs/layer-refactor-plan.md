# Layer refactor plan

Goal: bring the app in line with `AGENTS.md` / `.cursorrules` one slice at a time, without a big-bang rewrite and without wiping local data.

Ground rules for every phase:
- **No Room schema change** unless a phase says so. `ShuttleDatabase` uses `fallbackToDestructiveMigration`, so a version bump deletes unsynced passes on drivers' phones.
- Each phase ends with a green `./gradlew assembleDebug testDebugUnitTest` and is shippable on its own (one commit or PR per phase).
- Only touch the files the phase lists. Old patterns in other screens stay until their own phase.

---

## Phase 1: `ShuttlePassRepository` speaks domain models — ✅ done

Also covered `PassengerQRRepository` (types only), so screens don't mix entity and domain passenger types.

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

## Phase 2: Sync screen (`ShuttlePassListScreen`) to MVI — ✅ done (structure only)

**Scope change:** MVI restructure only. The look is unchanged. The mockup 08/12/13 restyle waits until the design-system tokens (Inter, Midnight/Mint colors, type scale) are added to `ui/theme`.

What landed:
- `presentation/state/ShuttlePassListState.kt` (`ShuttlePassListState`, `ShuttlePassItemUi`, `SyncStatus`), and `presentation/events/ShuttlePassListAction.kt` / `ShuttlePassListEvent.kt`.
- `ShuttlePassListViewModel`: reads `driverId` from `SavedStateHandle["id"]`, uses `filter.flatMapLatest { observePasses }` (fixing the old `FilterViewModel` collector leak), and resolves route names once per id. It formats rows, using each pass's own month for the `SPT-…` code. One `StateFlow`, plus a Channel for navigation events.
- `ShuttlePassListRoot` / `ShuttlePassListScreen(state, onAction)`. A stateless `components/ShuttlePassCard` (a visual copy of HomeScreen's `ShuttlePassListItem`). `components/ObserveAsEvents`. `TopBarComponent` gained an `onBack` lambda, and `navController` is now optional.
- Deleted `FilterViewModel` and `FilterState`. `UploadUiState` stays for `ShuttlePassItemScreen` (Phase 4). The old `ShuttlePassListState` used by HomeScreen was renamed `ShuttlePassListLegacyState` (removed in Phase 6).
- Tests: `kotlinx-coroutines-test` + Turbine. `ShuttlePassListViewModelTest` (5 cases), `MainDispatcherRule`, and fakes in `app/src/test/.../testutil/`.

**Follow-up (done):** The androidTest classpath conflict is fixed. AGP pins androidTest dependencies to the app's runtime versions, and CameraX 1.5.1 resolved `androidx.concurrent:concurrent-futures` to 1.1.0, while `androidx.test` (junit 1.3.0 / espresso 3.7.0) needs 1.2.0. The app now declares `concurrent-futures` 1.2.0 (`libs.androidx.concurrent.futures`). `ShuttlePassDaoTest` (`app/src/androidTest/.../data/local/dao/`) is an in-memory Room test covering `markSynced`, drafts excluded from `countLateShuttle`, and `getAllShuttlePassFilteredByLate` matching the count. It uses `room-testing` 2.7.1 and `runBlocking`. `kotlinx-coroutines-test` 1.10.2 can't go on androidTest while the app runtime resolves coroutines 1.8.1, because the same strict alignment applies. It compiles, `lintDebug` passes, and all 3 cases pass on the Pixel_9 API 35 emulator (`./gradlew connectedDebugAndroidTest`).

---

## Phase 3: Network config + offline-first session — ✅ done (app side)

Offline-first rules this phase locks in:
- After one online login, the driver **stays signed in until an explicit logout**, with or without internet. "Remember me" is gone. Drivers who used it before the update are migrated automatically from the `LoggedUser` table.
- Only **login** and **upload** need the network. An expired or rejected token **never signs the driver out** and never deletes data. It shows "Sign in again to upload", and a password re-auth dialog refreshes the token in place. Re-auth must be the same account.
- **Logout** warns when passes aren't uploaded ("Upload first" / "Log out anyway"). It clears the session and cached profile and keeps every pass.

What landed:
- `NetworkModule`: one OkHttp + one Retrofit, with timeouts. Logging is `BODY` only in debug, and the `Authorization` header is always redacted.
- `BASE_URL` from `BuildConfig`: debug `http://10.0.2.2:5105/api/`, release `-PshuttleApiUrl=https://…` (the build fails if the URL isn't https). Cleartext is allowed only in the debug build, and only for `10.0.2.2`/`localhost`; the global `usesCleartextTraffic` was removed.
- `SessionRepository` (domain) / `SessionRepositoryImpl` + `EncryptedSessionStore` (EncryptedSharedPreferences, excluded from backup). `AuthInterceptor` adds the Bearer token and marks it expired on 401. `NetworkMonitor` (domain) / `ConnectivityNetworkMonitor`.
- `MainActivity` picks the start destination from the stored session. The pass list shows offline / sign-in-again banners and the `ReauthDialog`.
- Tests: `SessionRepositoryImplTest`, `AuthInterceptorTest` (MockWebServer), `LogoutUseCaseTest`, and the extended `ShuttlePassListViewModelTest`.

**Backend:** still to do. See `docs/backend-auth-contract.md` (token + `expiresAt` on login, `[Authorize]` + 401, long-lived token, upsert by pass id). Until then the app runs unauthenticated, as before.

---

## Phase 3b: Background auto-sync (later)

WorkManager + Hilt-Work: upload unsynced passes automatically on reconnect (network constraint) and periodically. It reuses `ShuttlePassRepository.syncUnsynced`, and skips the run while `TokenStatus.Expired` is set (showing a notification to sign in instead). Needs the backend's idempotent upload (contract §5).

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
