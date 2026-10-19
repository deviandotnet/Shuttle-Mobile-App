---
name: android-shuttle-add-screen
description: >
  End-to-end workflow for adding a new screen (or rebuilding an existing one) in the Shuttle app,
  from domain model and repository through Hilt wiring, MVI ViewModel, Root/Screen composables
  styled from the redesign mockups, a type-safe route in Navigation.kt, tests, and a build check.
  Use this skill whenever the user asks to add, build, create, implement, rebuild or redesign a
  screen or feature end to end (e.g. the sync screen, manifest, dashboard, add passenger). Trigger
  on phrases like "add a screen", "new screen", "build the X screen", "implement the mockup",
  "redesign the X screen", "new feature", or "refactor X screen to MVI".
---

# Add or Rebuild a Screen

This is an ordered workflow. Each step points to the skill that holds the detailed rules. Follow `AGENTS.md`'s sequence: analyse, outline the plan, make targeted edits, and handle Loading, Success and Error.

## Project context

- The mockups are in `.claude/shuttle-mobile-redesign-v2/`:

  | # | Screen | # | Screen |
  |---|---|---|---|
  | 01 | welcome | 08 | shuttle-pass-list |
  | 02 | sign-in | 09 | delete-confirmation |
  | 03 | create-account | 10 | add-passenger |
  | 04 | navigation-drawer | 11 | complete-confirmation |
  | 05 | dashboard | 12 | sync-success |
  | 06 | empty-shuttle-pass | 13 | sync-error |
  | 07 | passenger-manifest | 14 | qr-scanner |

- Visual rules come from `DESIGN-SYSTEM.md`, in the same folder.
- Domain glossary (`AGENTS.md`): Shuttle, Route, Passenger, Manifest.

## Workflow

### 0. Analyse and confirm
- Open the matching mockup PNG(s) and `DESIGN-SYSTEM.md`.
- Find what already exists: the screen, ViewModel, state and repository methods. For a rebuild, list the current behaviour that must be preserved.
- If the data it needs, or the backend endpoint, is unclear, **ask** before writing code.
- Outline the plan in a few bullets.

### 1. Domain (`domain/`) → **android-project-structure**, **android-error-handling**
- [ ] Add a model in `domain/model/` if needed (pure Kotlin, `val`s).
- [ ] Add or extend the `XRepository` interface in `domain/repository/`, using domain types only.
- [ ] Add a use case or validator in `domain/usecase/<feature>/` that returns an `XValidationResult` or `Resource`.

### 2. Data (`data/`) → **android-data-layer**
- [ ] DTO with `@SerializedName` matching the backend camelCase, plus a Retrofit method returning `Response<T>`.
- [ ] Entity and DAO if the data is persisted, registered in `config/Database.kt`. **Flag the version bump**, because it wipes unsynced passes.
- [ ] Mappers in `data/mappers/`.
- [ ] `XRepositoryImpl` with IO on `Dispatchers.IO` and errors returned as `Resource`.

### 3. DI (`di/`) → **android-di-hilt**
- [ ] `@Binds` in `RepositoryModule` for the new repository.
- [ ] `@Provides` in `UseCaseModule` for each new use case.

### 4. Presentation logic → **android-presentation-mvi**, **android-state-management**
- [ ] `presentation/state/XState.kt` (+ `XUi` display models).
- [ ] `presentation/events/XAction.kt` and `XEvent.kt`.
- [ ] `presentation/viewmodel/XViewModel.kt`: `@HiltViewModel`, one `StateFlow`, a `Channel` for events, `onAction`, and `SavedStateHandle` for arguments and critical form fields.
- [ ] If the screen currently lives on `ShuttleViewModel`, move only this screen's logic into its own ViewModel.

### 5. UI → **android-compose-ui**
- [ ] `presentation/XScreen.kt` contains `XRoot(viewModel, nav lambdas)` and `XScreen(state, onAction)`.
- [ ] Match the mockup using theme tokens. Add any missing token to `ui/theme` rather than inlining hex or sp values.
- [ ] Render loading, error, empty and content.
- [ ] Extract reused pieces into `presentation/components/`.
- [ ] Add `@Preview`s in `ShuttleAppTheme`: the main state, plus empty and error.

### 6. Navigation → **android-navigation**
- [ ] Add a `@Serializable` route in `Navigation.kt`. Apply the serialization plugin the first time.
- [ ] Add `composable<XRoute> { XRoot(viewModel = hiltViewModel(), …) }` and wire the lambdas, with `popUpTo` where Back must not return.
- [ ] Update the callers: drawer items and other screens' `onNavigate…` lambdas.

### 7. Tests → **android-testing**
- [ ] Validator tests.
- [ ] ViewModel test with fakes: success, error and validation paths, plus the events.
- [ ] A DAO test if queries were added or changed.

### 8. Verify
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lint
```
- [ ] If the backend is running on port 5105, run the app on an emulator and walk the flow. Otherwise, say that it wasn't run.
- [ ] Check that no new `presentation`/`domain` → `data` imports were added:

```bash
grep -rn "import com.example.shuttleapp.data" app/src/main/java/com/example/shuttleapp/presentation app/src/main/java/com/example/shuttleapp/domain
```

## Don'ts

- Don't refactor unrelated screens or mass-rename legacy classes in the same change.
- Don't pass `NavController` or ViewModels into `XScreen`.
- Don't bump the Room version without telling the user about the data wipe.
- Don't add libraries (Koin, Ktor, MockK, …) that the project doesn't use.

## Related skills

**android-project-structure**, **android-data-layer**, **android-error-handling**, **android-di-hilt**, **android-presentation-mvi**, **android-state-management**, **android-compose-ui**, **android-navigation**, **android-testing**, **android-edge-to-edge**
