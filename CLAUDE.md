# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Project rules, persona, and the domain glossary live in `AGENTS.md`; Cursor rules live in `.cursorrules`. Both are imported here and are binding:

@AGENTS.md
@.cursorrules

Package layout, key file locations, and the redesign mockups (`.claude/shuttle-mobile-redesign-v2/`, with `DESIGN-SYSTEM.md`) are documented in `AGENTS.md`.

## Commands

Single-module Gradle project (`:app`), Kotlin DSL, version catalog at `gradle/libs.versions.toml`. On Windows use `./gradlew` from Git Bash or `.\gradlew.bat` from PowerShell.

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # build + install on connected device/emulator
./gradlew lint                          # Android lint
./gradlew testDebugUnitTest             # JVM unit tests (app/src/test)
./gradlew testDebugUnitTest --tests "com.example.shuttleapp.ExampleUnitTest"   # single test class
./gradlew connectedDebugAndroidTest     # instrumented tests (needs a device)
```

Only the template example tests exist today; there is no real test suite yet.

The backend is a separate C# ASP.NET Core API. `API.BASE_URL` is `http://10.0.2.2:5105/api/` — the emulator alias for the host machine — so the backend must be running locally on port 5105 for login/register/sync to work.

## Architecture (big picture)

**Flow:** Compose screen → `@HiltViewModel` → domain repository interface (`domain/repository`) / validation use cases (`domain/usecase`) → `data/repositoryImpl` → Room DAOs (`data/local/dao`) and Retrofit APIs (`data/network`: `AuthApi`, `ShuttleApi`). Bindings from interfaces to impls are in `di/RepositoryModule.kt`; use cases are provided manually in `di/UseCaseModule.kt` (add a `@Provides` there when creating a new one).

**Navigation:** `Navigation.kt` defines a string-based `sealed class Route` and a single `NavHost`. Most routes take an `/{id}` argument (the logged-in user/driver id or a shuttle pass id). ViewModels are obtained with `hiltViewModel()` inside each `composable {}` block, so they are scoped per back-stack entry — `ShuttleViewModel` is *not* shared across screens.

**Offline-first shuttle pass lifecycle** (the core of the app, spread across `ShuttlePassEntity`, `ShuttlePassDao`, `ShuttlePassRepositoryImpl`, `ShuttleViewModel`):
1. A driver's in-progress pass is a row with `isDraft = true`; scanned passengers (`PassengerQREntity`) attach to it via `shuttlePassId`. There is at most one draft per driver (`getShuttlePassIdDraftByDriverId`).
2. `finalizeShuttleDraft` flips the pass and its passengers to `isDraft = false`.
3. Finalized, unsynced passes (`isDraft = 0 AND isSynced = 0`) are mapped to `ShuttlePassWithPassengerDto` and POSTed to `shuttlepasses/`; on success they are marked `isSynced`.
4. Synced passes older than one month are purged by `deleteShuttlePassOnDue`.
`isLateShuttle` drives the "late" filter/counter. DAO queries filter by the `driver` column, which holds the account id.

**Auth/session:** `AuthRepositoryImpl.postLogin` returns `Flow<Resource<UserData>>`, checks the backend's `message == "login successful"`, and caches the user's shuttle provider and user data in Room. "Remember me" is persisted via `RememberedUserEntity`. Logout goes through `LogoutUseCase`.

**Mapping:** `data/mappers` holds extension functions (`toEntity`, `toDto`, `toDomain`-style) between DTOs, Room entities, and domain models. DTOs currently use Gson (`GsonConverterFactory`); `.cursorrules` prefers kotlinx.serialization for *new* DTOs.

**Room:** `ShuttleDatabase` is at `version = 26` with `exportSchema = false` and `fallbackToDestructiveMigration` — bumping the version on a schema change wipes local data (including unsynced passes) rather than migrating. Register new entities/DAOs in `config/Database.kt`.

## Known gaps vs. the rules

The existing code does not yet fully follow `AGENTS.md`; don't copy these patterns into new code, and don't mass-refactor them unless asked:
- Domain repository interfaces (e.g. `ShuttlePassRepository`) expose `data` entities and DTOs, and several screens/ViewModels import from `com.example.shuttleapp.data` directly.
- Screens receive ViewModels and `NavController` as parameters instead of hoisted state + lambdas.
- `ShuttleViewModel` (~900 lines) exposes several separate `StateFlow`s rather than a single UI state.
- `AppModule` uses `HttpLoggingInterceptor.Level.BODY` unconditionally, and no Bearer-token interceptor exists yet.
