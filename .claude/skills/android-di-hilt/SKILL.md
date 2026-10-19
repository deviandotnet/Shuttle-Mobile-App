---
name: android-di-hilt
description: >
  Hilt dependency injection for the Shuttle app - AppModule singletons (Retrofit, OkHttp, Room),
  @Binds repository bindings in RepositoryModule, use case provisioning in UseCaseModule,
  @HiltViewModel injection, and hiltViewModel() scoping inside the NavHost. Use this skill whenever
  adding or injecting a dependency, providing a repository, API, DAO or use case, creating a
  ViewModel, or touching anything in the di package. Trigger on phrases like "inject",
  "DI module", "provide a repository", "bind the interface", "@HiltViewModel", "hiltViewModel",
  "@Provides", "@Binds", "@Inject", "AppModule", "RepositoryModule", or "UseCaseModule".
---

# Dependency Injection (Hilt)

## Project context

| File | Responsibility |
|---|---|
| `ShuttleApp.kt` | `@HiltAndroidApp` application class |
| `MainActivity.kt` | `@AndroidEntryPoint` host for `Navigation()` |
| `di/AppModule.kt` | `@Singleton` infrastructure: OkHttp, Retrofit APIs (`AuthApi`, `ShuttleApi`), `ShuttleDatabase` |
| `di/RepositoryModule.kt` | `abstract class` with `@Binds @Singleton` for every `XRepositoryImpl → XRepository` |
| `di/UseCaseModule.kt` | `@Provides` functions for domain use cases and validators |
| `Navigation.kt` | Calls `hiltViewModel<XViewModel>()` inside each `composable {}` block |

All modules use `@InstallIn(SingletonComponent::class)` and live in the `di` package.

## Rules

- Use Hilt for all injection. No manual singletons, service locators or `object` holders for dependencies.
- Domain classes stay framework-free: use cases are plain Kotlin classes. `javax.inject.Inject` is the only DI annotation they may carry.
- Bind **interfaces**, not implementations. ViewModels depend on `domain/repository` interfaces and use cases, never on `RepositoryImpl`, DAOs, APIs or `ShuttleDatabase`.
- Call `hiltViewModel()` **only inside the `composable {}` block** in `Navigation.kt` (per `.cursorrules`), so each ViewModel is scoped to its back-stack entry. Pass the ViewModel into the screen's `XRoot` composable and no further.
- ViewModels are not shared between destinations. Each `hiltViewModel<ShuttleViewModel>()` call creates a separate instance.

## Patterns

### ViewModel

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,             // domain interface
    private val rememberedUserRepository: RememberedUserRepository,
    private val validateLoginInput: ValidateLoginInput        // use case
) : ViewModel()
```

Navigation arguments can be read from an injected `SavedStateHandle` instead of being passed through the composable:

```kotlin
@HiltViewModel
class ShuttlePassDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shuttlePassRepository: ShuttlePassRepository
) : ViewModel() {
    private val passId = savedStateHandle.toRoute<ShuttlePassDetailRoute>().id
}
```

### Repository binding (`RepositoryModule`)

```kotlin
class ShuttlePassRepositoryImpl @Inject constructor(
    private val database: ShuttleDatabase,
    private val shuttleApi: ShuttleApi
) : ShuttlePassRepository { /* ... */ }

// di/RepositoryModule.kt
@Binds
@Singleton
abstract fun bindShuttlePassRepository(
    shuttlePassRepositoryImpl: ShuttlePassRepositoryImpl
): ShuttlePassRepository
```

### Use cases (`UseCaseModule`)

The project provides use cases explicitly in `UseCaseModule`. Add a `@Provides` function whenever you create a use case:

```kotlin
@Provides
fun provideLogoutUseCase(userDataRepository: UserDataRepository): LogoutUseCase =
    LogoutUseCase(userDataRepository = userDataRepository)
```

A use case with an `@Inject constructor` (like `LogoutUseCase`) would also resolve without a `@Provides`. Do not do both for the same class. For consistency, new use cases get a plain constructor and a `@Provides` in `UseCaseModule`.

### Infrastructure (`AppModule`)

Add new Retrofit APIs, DAOs-as-dependencies or other singletons here:

```kotlin
@Singleton
@Provides
fun providesShuttleApi(retrofit: Retrofit): ShuttleApi = retrofit.create(ShuttleApi::class.java)
```

When adding infrastructure, prefer providing one shared `OkHttpClient` and one `Retrofit` and deriving each API from them. Today `AppModule` builds a separate `Retrofit` per API, which works but duplicates config.

## Legacy code / known gaps

Don't copy these patterns into new code, and don't mass-refactor them unless asked:

- `AppModule` sets `HttpLoggingInterceptor.Level.BODY` unconditionally. `.cursorrules` requires `if (BuildConfig.DEBUG) BODY else NONE`. That needs `buildFeatures { buildConfig = true }`.
- There is no Bearer-token interceptor yet. `.cursorrules` requires one for every call except login/register. Add it as an OkHttp `Interceptor` provided from `AppModule` once token storage exists (see **android-data-layer**).
- `AppModule` imports unused use-case classes. Use cases belong in `UseCaseModule`.
- Some screens take a default `viewModel: XViewModel = hiltViewModel()` parameter (e.g. `LoginScreen`). New screens get the ViewModel from the `NavHost` instead.

## Checklist: wiring a new dependency

- [ ] Repository: the interface is in `domain/repository`, `XRepositoryImpl @Inject constructor` is in `data/repositoryImpl`, and the `@Binds @Singleton` is in `RepositoryModule`.
- [ ] Use case: the class is in `domain/usecase/<feature>/` and has a `@Provides` in `UseCaseModule`.
- [ ] API, database or other infrastructure: a `@Singleton @Provides` is in `AppModule`.
- [ ] ViewModel: `@HiltViewModel` + `@Inject constructor`, and it depends only on domain interfaces and use cases.
- [ ] `hiltViewModel<XViewModel>()` is called inside the route's `composable {}` block and passed to `XRoot`.

## Related skills

- **android-project-structure**: where each class lives
- **android-data-layer**: repository implementations, APIs, Room
- **android-presentation-mvi**: the ViewModel and Root/Screen split
- **android-navigation**: `composable {}` blocks and route arguments
- **android-testing**: constructing ViewModels with fakes, without Hilt
