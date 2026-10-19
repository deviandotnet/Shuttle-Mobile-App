---
name: android-navigation
description: >
  Navigation for the Shuttle app - the single NavHost in Navigation.kt (Navigation-Compose 2.9),
  migrating string routes to type-safe @Serializable routes one at a time, passing ids, scoping
  hiltViewModel() per destination, nav lambdas instead of NavController, and back stack handling
  (popUpTo for login -> home and logout). Use this skill whenever adding a screen or route,
  navigating between screens, passing arguments, or editing Navigation.kt. Trigger on phrases like
  "add a route", "navigate to", "NavHost", "NavController", "type-safe navigation",
  "navigation argument", "popUpTo", "back stack", or "Navigation.kt".
---

# Navigation (Navigation-Compose 2.9)

## Project context

- `Navigation.kt` holds the only `NavHost`. Start destination: the login screen.
- Today's routes use `sealed class Route(val route: String)` with an `"/{id}"` suffix. The id is the logged-in account or driver id, or a shuttle pass id.
- Dependencies: `androidx.navigation:navigation-compose:2.9.0`, `androidx.hilt:hilt-navigation-compose:1.3.0`, and kotlinx-serialization-json, which is already on the classpath. Type-safe routes also need the `org.jetbrains.kotlin.plugin.serialization` Gradle plugin. Add it the first time a `@Serializable` route is introduced.
- The redesign's drawer (`04-navigation-drawer.png`) navigates between Home, Shuttle Pass, Manifest/List and Sync.

## Rules

- Keep **one `NavHost`** in `Navigation.kt`. No nested feature graphs; the app is small.
- New routes are **type-safe** (`.cursorrules`): a `@Serializable data object` for no arguments, or a `@Serializable data class` for arguments. String and typed routes can coexist in the same `NavHost`, so **migrate one route at a time** when its screen is touched.
- Call `hiltViewModel<XViewModel>()` **inside the `composable<…> {}` block** and pass it to `XRoot`. Never call it deeper.
- `XRoot` receives **lambdas** (`onNavigateToHome: (String) -> Unit`, `onBack: () -> Unit`). Only `Navigation.kt` touches `NavController`.
- Pass **ids, not objects**. The destination ViewModel loads data by id, from `SavedStateHandle.toRoute<…>()`.
- After login, and after logout, clear the back stack so Back can't return to an authenticated or unauthenticated screen.

## Patterns

### Routes

Keep them in `Navigation.kt` next to the legacy `Route` class until it's gone:

```kotlin
@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data class HomeRoute(val accountId: String)
@Serializable data class ShuttlePassListRoute(val driverId: String)
@Serializable data class ShuttlePassDetailRoute(val shuttlePassId: String)
@Serializable data class QrScannerRoute(val shuttlePassId: String)
```

### Destinations

```kotlin
NavHost(navController = navController, startDestination = LoginRoute) {

    composable<LoginRoute> {
        LoginRoot(
            viewModel = hiltViewModel<LoginViewModel>(),
            onNavigateToHome = { accountId ->
                navController.navigate(HomeRoute(accountId)) {
                    popUpTo<LoginRoute> { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToRegister = { navController.navigate(RegisterRoute) }
        )
    }

    composable<ShuttlePassDetailRoute> {
        // the ViewModel reads shuttlePassId via savedStateHandle.toRoute<ShuttlePassDetailRoute>()
        ShuttlePassDetailRoot(
            viewModel = hiltViewModel<ShuttlePassDetailViewModel>(),
            onBack = navController::navigateUp,
            onEdit = { id -> navController.navigate(EditShuttlePassRoute(id)) }
        )
    }
}
```

If the composable itself needs the argument, use `val route = backStackEntry.toRoute<ShuttlePassDetailRoute>()`.

### Logout

```kotlin
onLogout = {
    navController.navigate(LoginRoute) {
        popUpTo(navController.graph.id) { inclusive = true }
    }
}
```

### Migrating a legacy string route

Google's detailed Navigation 2 guide is [`navigation-3/references/android/guide/navigation/type-safe-destinations.md`](../navigation-3/references/android/guide/navigation/type-safe-destinations.md). Per route:

1. Add the `@Serializable` route.
2. Replace `composable(route = Route.X.route + "/{id}", arguments = …)` with `composable<XRoute>`.
3. Update every `navController.navigate(Route.X.route + "/$id")` call to `navigate(XRoute(id))`. Search for `Route.X.route`.
4. Delete the `Route.X` entry once nothing references it.

## Legacy code / known gaps

Don't copy these patterns into new code, and don't mass-refactor them unless asked:

- String routes with `navArgument("id") { defaultValue = "" }`. An empty id silently loads nothing.
- Screens take `navController: NavController` and build route strings themselves (`LoginScreen` → `Route.HomeScreen.route + "/${it.id}"`).
- `LoginScreen` defaults `loginViewModel: LoginViewModel = hiltViewModel()` instead of receiving it from the `NavHost`.
- `QRScannerScreen(id, navController)` gets no ViewModel from the `NavHost`.

## Checklist: adding or migrating a route

- [ ] There is a `@Serializable` route object or class, with only ids or primitives as arguments.
- [ ] The serialization Gradle plugin is applied (first typed route only).
- [ ] `composable<XRoute> { XRoot(viewModel = hiltViewModel(), on… = { navController.navigate(…) }) }` is in place.
- [ ] The ViewModel reads its arguments via `SavedStateHandle.toRoute<XRoute>()`.
- [ ] `popUpTo` is used where Back must not return (login, logout, finishing a pass).
- [ ] No `NavController` exists below `Navigation.kt`.

## Related skills

- **android-presentation-mvi**: `XRoot` and events that trigger navigation
- **android-di-hilt**: `hiltViewModel()` scoping
- **navigation-3**: Google's guide to Jetpack Navigation 3. This app is on Navigation 2.9, so use it only if a Nav3 migration is explicitly requested. Its `type-safe-destinations.md` reference does apply to the Navigation 2 route migration.
