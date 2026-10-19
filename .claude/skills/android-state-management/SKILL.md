---
name: android-state-management
description: >
  State and coroutine rules for the Shuttle app - StateFlow for screen state, Channel for one-time
  events, atomic .update {} mutations, loading/error/empty/content states, viewModelScope, where
  Dispatchers.IO belongs, Flow vs suspend, combining Room flows, and lifecycle-aware collection in
  Compose. Use this skill whenever managing screen state, collecting or transforming flows,
  launching coroutines from a ViewModel, or handling loading and error UI. Trigger on phrases like
  "StateFlow", "SharedFlow", "Channel", "loading state", "error state", "empty state",
  "coroutine", "viewModelScope", "Dispatchers.IO", "collect", "combine", "stateIn", or
  "collectAsStateWithLifecycle".
---

# State Management & Coroutines

## Project context

- Screen state classes are in `presentation/state/`, and ViewModels in `presentation/viewmodel/`.
- Repositories return `Flow<Resource<T>>`, `suspend … : Resource<T>` or Room `Flow<T>` (see **android-error-handling**).
- `collectAsStateWithLifecycle()` is available: `lifecycle-runtime-compose` comes in transitively via `navigation-compose`. Declare it in the version catalog if you want it explicit.

## Rules

1. **Expose state as one read-only `StateFlow`:** `private val _state = MutableStateFlow(XState())` and `val state = _state.asStateFlow()`. Never expose `MutableStateFlow`.
2. **Mutate with `_state.update { it.copy(...) }`**, never `_state.value = _state.value.copy(...)`, which loses updates under concurrency. State classes use `val` only.
3. **Use a `Channel` for one-time events:** `Channel<XEvent>()` + `receiveAsFlow()`, consumed with `ObserveAsEvents` (see **android-presentation-mvi**).
4. **Use `viewModelScope.launch` with no dispatcher argument.** ViewModels run on Main. **Repositories own the IO switch** (`withContext(Dispatchers.IO)` / `.flowOn(Dispatchers.IO)`), which satisfies the `AGENTS.md` rule that network calls run on `Dispatchers.IO` without leaking the dispatcher into the UI layer.
5. **Never use `runBlocking`, `GlobalScope` or ad-hoc `CoroutineScope(...)`** in app code.
6. **Never use `LiveData`** and never put `mutableStateOf` app state in a ViewModel.
7. **Collect in Compose with `collectAsStateWithLifecycle()`**, not `collectAsState()`.
8. **Never collect a flow inside a click handler.** Send an action, and let the ViewModel launch the work.
9. When starting an operation, **clear the error**, and set `isLoading = false` in both the success and error branches.

## Patterns

### Loading, error, empty and content

```kotlin
data class ShuttlePassListState(
    val passes: List<ShuttlePassUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isSyncing: Boolean = false,         // separate flag for a separate operation
    val syncErrorMessage: String? = null
)
```

```kotlin
when {
    state.isLoading -> LoadingIndicator()
    state.errorMessage != null -> ErrorMessage(state.errorMessage, onRetry = { onAction(OnRetryClick) })
    state.passes.isEmpty() -> EmptyShuttlePass()          // see mockup 06-empty-shuttle-pass.png
    else -> ShuttlePassList(state.passes, onAction)
}
```

Use one flag pair per independent operation (load vs sync) rather than one global `isLoading`.

### Observing Room into state

```kotlin
init {
    shuttlePassRepository.observeShuttlePasses(driverId)
        .combine(filterFlow) { passes, filter -> passes.filter(filter::matches).map { it.toUi() } }
        .onEach { list -> _state.update { it.copy(passes = list, isLoading = false) } }
        .catch { e -> _state.update { it.copy(isLoading = false, errorMessage = "Could not load shuttle passes.") } }
        .launchIn(viewModelScope)
}
```

Alternatively, derive the whole state with `stateIn`:

```kotlin
val state: StateFlow<HomeState> = combine(
    shuttlePassRepository.countUnsyncedShuttlePass(accountId),
    shuttlePassRepository.countLateShuttle(accountId)
) { unsynced, late -> HomeState(unsyncedCount = unsynced, lateCount = late, isLoading = false) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())
```

Use `stateIn` when the state is **purely derived** from flows, and `MutableStateFlow` + `update` when actions also mutate it.

### Flow vs suspend

| Scenario | Use |
|---|---|
| Room data the screen observes (passes, counters, draft) | `Flow<T>` from the DAO |
| A network call that shows progress | `Flow<Resource<T>>` |
| A one-shot command (sync, delete, finalize) | `suspend fun …: Resource<T>` |
| Screen state | `StateFlow<XState>` |
| Navigation, toast or snackbar | `Channel<XEvent>` |

Don't wrap a one-shot fetch in a `Flow` just for consistency.

## Legacy code / known gaps

Don't copy these patterns into new code, and don't mass-refactor them unless asked:

- `viewModelScope.launch(Dispatchers.IO)` in `LoginViewModel`, `RegisterViewModel`, `QrAnalyzerViewModel` and `ShuttleViewModel`, with the dispatcher in the ViewModel instead of the repository.
- Publicly exposed `MutableStateFlow`s (`ShuttleViewModel.userState`, `QrAnalyzerViewModel.qrInputState`).
- `MutableSharedFlow` for `ShuttlePassUiEvent`.
- `collectAsState()` in existing screens.
- `try { … } catch (ex: Exception) { ex.printStackTrace() }` around flow collection.

## Checklist

- [ ] There is one `StateFlow<XState>`, exposed read-only.
- [ ] All mutations go through `.update {}`.
- [ ] Events go through a `Channel` + `receiveAsFlow()`.
- [ ] There's no dispatcher in `viewModelScope.launch`, and the repository handles IO.
- [ ] Loading, error, empty and content are all rendered.
- [ ] The UI collects with `collectAsStateWithLifecycle()`.

## Related skills

- **android-presentation-mvi**: the State, Action and Event structure
- **android-error-handling**: `Resource` and error messages
- **android-testing**: testing flows with Turbine
