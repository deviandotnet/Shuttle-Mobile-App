---
name: android-presentation-mvi
description: >
  MVI presentation layer for the Shuttle app - one XState data class per screen, XAction (user
  intents), XEvent (one-time effects via Channel), a @HiltViewModel exposing a single StateFlow,
  the XRoot / XScreen composable split with state hoisting, UI models, and SavedStateHandle.
  Use this skill whenever creating or reviewing a ViewModel or screen, defining screen state,
  actions or events, splitting ShuttleViewModel, or wiring a screen to navigation. Trigger on
  phrases like "add a ViewModel", "create a screen", "MVI", "UI state", "action", "event",
  "Root composable", "state hoisting", "ObserveAsEvents", "SavedStateHandle", or "UI model".
---

# Presentation Layer (MVI)

## Project context

| Path | Contents |
|---|---|
| `presentation/XScreen.kt` | `XRoot` and `XScreen` composables, in the same file |
| `presentation/state/XState.kt` | Screen state data class, plus `XUi` display models |
| `presentation/events/XAction.kt`, `XEvent.kt` | User intents and one-time effects |
| `presentation/viewmodel/XViewModel.kt` | `@HiltViewModel` |
| `presentation/components/` | Reusable composables, including `ObserveAsEvents` |
| `Navigation.kt` | Creates the ViewModel with `hiltViewModel()` and calls `XRoot` |

## Rules

From `AGENTS.md`, binding:

- Each screen has **one** `XState` data class describing the whole screen, and the ViewModel exposes it as **one** `StateFlow<XState>` (private `MutableStateFlow`, public `asStateFlow()`).
- **State hoisting:** ViewModels are never passed below `XRoot`. `XScreen` receives only `state` and `onAction`.
- **No business logic in composables.** Formatting, date parsing and filtering happen in the ViewModel or domain before reaching the UI.
- **No `data` imports** in presentation. Talk to domain repository interfaces and use cases only.
- Handle **Loading, Success and Error** in every screen that loads data.
- Composables stay under about 100 lines. Split them into private sub-composables or `presentation/components`.

Naming, agreed for new and refactored screens:

| Thing | Name | Example |
|---|---|---|
| State | `XState` | `LoginState` |
| User intents | `XAction` (sealed interface) | `LoginAction` |
| One-time effects | `XEvent` (sealed interface) | `LoginEvent` |
| ViewModel | `XViewModel` | `LoginViewModel` |
| Stateful entry composable | `XRoot` | `LoginRoot` |
| Stateless composable | `XScreen` | `LoginScreen` |
| Display model | `XUi` | `ShuttlePassUi` |

## Patterns

### State, Action, Event

```kotlin
// presentation/state/LoginState.kt
data class LoginState(
    val username: String = "",
    val password: String = "",
    val isPasswordShown: Boolean = false,
    val isRemembered: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// presentation/events/LoginAction.kt
sealed interface LoginAction {
    data class OnUsernameChange(val value: String) : LoginAction
    data class OnPasswordChange(val value: String) : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data class OnRememberChange(val checked: Boolean) : LoginAction
    data object OnLoginClick : LoginAction
    data object OnRegisterClick : LoginAction
}

// presentation/events/LoginEvent.kt
sealed interface LoginEvent {
    data class NavigateToHome(val accountId: String) : LoginEvent
    data object NavigateToRegister : LoginEvent
}
```

- Put what is *on screen* (including error text) in state.
- Use events only for things that must happen **once**: navigation, a toast or snackbar, closing a dialog.

### ViewModel

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateLoginInput: ValidateLoginInput
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnUsernameChange -> _state.update { it.copy(username = action.value, errorMessage = null) }
            is LoginAction.OnPasswordChange -> _state.update { it.copy(password = action.value, errorMessage = null) }
            LoginAction.OnTogglePasswordVisibility -> _state.update { it.copy(isPasswordShown = !it.isPasswordShown) }
            is LoginAction.OnRememberChange -> _state.update { it.copy(isRemembered = action.checked) }
            LoginAction.OnLoginClick -> login()
            LoginAction.OnRegisterClick -> viewModelScope.launch { _events.send(LoginEvent.NavigateToRegister) }
        }
    }

    private fun login() {
        val s = _state.value
        val validation = validateLoginInput.execute(s.username, s.password)
        if (!validation.successful) {
            _state.update { it.copy(errorMessage = validation.errorMessage) }
            return
        }
        viewModelScope.launch {
            authRepository.postLogin(LoginCredentials(s.username, s.password)).collect { result ->
                when (result) {
                    is Resource.Loading -> _state.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        result.data?.let { _events.send(LoginEvent.NavigateToHome(it.accountId)) }
                    }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
```

- `viewModelScope.launch` runs on Main. The repository switches to `Dispatchers.IO` (see **android-state-management**).
- There's one public entry point, `onAction`, so no other public functions mutate state.
- Use `Channel` + `receiveAsFlow()` for events. Unlike `MutableSharedFlow()`, it buffers an event emitted while the UI isn't collecting, such as during a configuration change.

### `ObserveAsEvents` (add once to `presentation/components/ObserveAsEvents.kt`)

```kotlin
@Composable
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) { flow.collect(onEvent) }
        }
    }
}
```

### Root and Screen (same file)

```kotlin
// presentation/LoginScreen.kt
@Composable
fun LoginRoot(
    viewModel: LoginViewModel,                       // created in Navigation.kt
    onNavigateToHome: (accountId: String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LoginEvent.NavigateToHome -> onNavigateToHome(event.accountId)
            LoginEvent.NavigateToRegister -> onNavigateToRegister()
        }
    }
    LoginScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun LoginScreen(state: LoginState, onAction: (LoginAction) -> Unit) {
    // pure UI: render state, call onAction(...) on input
}

@Preview
@Composable
private fun LoginScreenPreview() {
    ShuttleAppTheme { LoginScreen(state = LoginState(username = "jdelacruz"), onAction = {}) }
}
```

`XRoot` takes navigation **lambdas**, never a `NavController` (see **android-navigation**).

### UI models

When a domain model needs display formatting, map it in the ViewModel:

```kotlin
data class ShuttlePassUi(
    val id: String,
    val routeName: String,
    val departureLabel: String,   // "07:30 AM"
    val passengerCount: Int,
    val isLate: Boolean,
    val isSynced: Boolean
)
```

Keep the date and time formatting in `util/DateTime.kt` or the domain layer, and call it from the ViewModel's mapper.

### Splitting `ShuttleViewModel`

`ShuttleViewModel` (about 900 lines) serves Home, the current pass, the pass list, pass details and editing, and exposes many `StateFlow`s. When you refactor one of those screens, give it its own `XViewModel` + `XState` that holds only what that screen needs, and leave the other screens on `ShuttleViewModel` until their turn. Don't split everything in one change.

### Process death

Store critical form input (e.g. the manual shuttle-pass form) in `SavedStateHandle`, and read the route's arguments from it:

```kotlin
@HiltViewModel
class EditShuttlePassViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val shuttlePassRepository: ShuttlePassRepository
) : ViewModel() {
    private val passId = savedStateHandle.toRoute<EditShuttlePassRoute>().id
    private val _state = MutableStateFlow(EditShuttlePassState(plateNumber = savedStateHandle["plateNumber"] ?: ""))
}
```

Save only what truly matters, not the entire state.

## Legacy code / known gaps

Don't copy these patterns into new code, and don't mass-refactor them unless asked:

- Screens receive `viewModel` and `navController` parameters and call `navController.navigate` directly (`LoginScreen`, `HomeScreen`, ...).
- ViewModels expose several flows, some typed as public `MutableStateFlow` (`ShuttleViewModel.userState`), or use `get() = _x` without `asStateFlow()`.
- `XInputEvent` classes play the role of `XAction`. Rename them to `XAction` when that screen is refactored.
- `ShuttlePassUiEvent` uses `MutableSharedFlow`. New events use `Channel`.
- State is mutated with `_state.value = _state.value.copy(...)`, and once in place with a `var` (`LoginState.loginErrorMessage`).
- `HomeScreen` takes `ShuttlePassWithPassengerEntity`, a `data` type, as a parameter.

## Checklist: a new or refactored screen

- [ ] `XState` is in `presentation/state`, immutable (`val`), with defaults, and includes `isLoading`/`errorMessage` where data loads.
- [ ] `XAction` and `XEvent` sealed interfaces are in `presentation/events`.
- [ ] `XViewModel` has one `StateFlow`, a `Channel` for events and a single public `onAction`, and depends only on domain.
- [ ] `XRoot` collects with `collectAsStateWithLifecycle`, observes events and takes nav lambdas.
- [ ] `XScreen(state, onAction)` is stateless and previewed inside `ShuttleAppTheme`.
- [ ] Display formatting happens in the ViewModel (`XUi` models), not the composable.
- [ ] `SavedStateHandle` holds critical form fields.

## Related skills

- **android-state-management**: flow and coroutine rules, loading, error and empty states
- **android-compose-ui**: composable quality, design tokens, previews
- **android-navigation**: calling `XRoot` from the `NavHost`
- **android-testing**: ViewModel tests with Turbine
- **android-shuttle-add-screen**: the end-to-end workflow
