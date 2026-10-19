---
name: android-compose-ui
description: >
  Jetpack Compose (Material 3) UI patterns for the Shuttle app - dumb composables, the redesign
  visual system (Inter, Midnight/Mint tokens, 8px grid) in ui/theme and presentation/components,
  stability and recomposition, side effects, lazy lists, animations, modifiers, previews,
  accessibility and text fields. Use this skill whenever writing, restyling or reviewing a
  composable, building a screen from the redesign mockups, adding a reusable component, or
  optimizing recomposition. Trigger on phrases like "composable", "restyle", "redesign",
  "mockup", "design tokens", "theme", "recomposition", "LaunchedEffect", "Modifier",
  "LazyColumn", "preview", "animation", "contentDescription", or "Compose performance".
---

# Compose UI

## Project context

| Path | Contents |
|---|---|
| `ui/theme/Color.kt`, `Type.kt`, `Theme.kt` | `ShuttleAppTheme`, colors and typography |
| `presentation/components/` | Reusable composables (`AuthButton`, `Cards`, `ModifiedTextField`, `SelectableDropdown`, `Snackbar`, `NavigationDrawerContent`, …) |
| `.claude/shuttle-mobile-redesign-v2/01-welcome.png` … `14-qr-scanner.png` | Target mockups |
| `.claude/shuttle-mobile-redesign-v2/DESIGN-SYSTEM.md` | **The source of truth** for type scale, color tokens and usage rules |

## Rules

- **The UI is dumb.** Composables render `state` and call `onAction`. They contain no business logic, data transformation or date parsing (`AGENTS.md`).
- **Use the design system, not ad-hoc values.** Colors and text styles come from `MaterialTheme`/`ui/theme`, and new tokens are named after `DESIGN-SYSTEM.md` (e.g. `Midnight = Color(0xFF07113F)`, `Mint = Color(0xFF55D6B4)`). Don't hard-code hex values or `fontSize = 18.sp` inside screens.
- **Key visual rules** (from `DESIGN-SYSTEM.md`):
  - Typeface: Inter, with tabular numerals for times, dates, IDs and passenger counts.
  - Primary CTA: Midnight, 12dp radius, at least 52dp tall. Inputs: white, 1dp border, 12dp radius, 52–56dp tall. Cards: white, 16dp radius, no cards nested in cards.
  - Layout: 8dp grid, 24dp page margins, 16–24dp section gaps, 48dp minimum touch target.
  - Colour meaning: Mint means scan, success or a positive action. Amber means pending sync. Red means error or destructive.
- **Material 3 only** (`androidx.compose.material3`). Don't add new usages of `androidx.compose.material` (M2) components.
- Keep composables **under about 100 lines**. Extract sub-composables, and move anything used on two screens into `presentation/components`.
- Every reusable composable takes `modifier: Modifier = Modifier` as its first optional parameter and applies it to the root.

## Patterns

### Theme tokens

When restyling, extend `ui/theme`:

```kotlin
// ui/theme/Color.kt
val Midnight = Color(0xFF07113F)
val Mint = Color(0xFF55D6B4)
val MintDark = Color(0xFF16A37A)
val WarmWhite = Color(0xFFFAF9F6)
val TextSecondary = Color(0xFF65769A)
val Warning = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFDC2626)
```

Map these into `lightColorScheme(primary = Midnight, secondary = Mint, background = WarmWhite, error = ErrorRed, …)` in `Theme.kt` and the type scale into `Typography` in `Type.kt`, so screens use `MaterialTheme.colorScheme.primary` and `MaterialTheme.typography.titleLarge`.

### A reusable component

```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) {
        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
```

Use slot parameters (`content: @Composable () -> Unit`) for flexible containers such as cards and scaffolds. Screen-level composables prefer typed parameters.

### Stability and recomposition

Strong skipping is on by default. Annotate a state class with `@Immutable` or `@Stable` only when it holds types the compiler treats as unstable (`List`, `Map`, interfaces) **and** a profile shows unnecessary recomposition. Pass lambdas such as `onAction` straight through rather than wrapping them in new lambdas in hot paths.

### State ownership

- App state lives in the ViewModel and is collected with `collectAsStateWithLifecycle()`.
- Only Compose-owned UI state uses `remember*` (`LazyListState`, `ScrollState`, `DrawerState`, `SnackbarHostState`, a dropdown's `expanded`).
- Use `derivedStateOf` only for values derived from that Compose-owned state (e.g. "show scroll-to-top").

### Side effects

Prefer an action handled by the ViewModel over a `LaunchedEffect`. Navigation and snackbars arrive as `XEvent`s through `ObserveAsEvents` (see **android-presentation-mvi**). Never write a `LaunchedEffect(state) { navController.navigate(…) }` that re-fires on recomposition. Lifecycle-bound work, such as the CameraX QR analyzer, goes in a dedicated composable with a `DisposableEffect`. Don't create custom `CompositionLocal`s.

### Lazy lists

```kotlin
LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)) {
    items(items = state.passes, key = { it.id }) { pass ->
        ShuttlePassCard(pass = pass, onClick = { onAction(ShuttlePassListAction.OnPassClick(pass.id)) })
    }
}
```

Always give a `key` when the item has a stable id. Shuttle passes and passengers both have one.

### Animations and modifiers

- Animate below composition: `animateFloatAsState` + `Modifier.graphicsLayer { alpha = … }`, or `offset { … }`. Avoid per-frame `Modifier.alpha(animated)`.
- Write modifier extensions as plain functions (`fun Modifier.cardSurface() = …`), never `@Composable`.

### Previews

Each `XScreen` has at least one `@Preview` wrapped in `ShuttleAppTheme`, using realistic shuttle data (route names, plate numbers, passenger counts). Add separate previews for the empty, error and loading states when the screen has them.

### Accessibility

- Give meaningful `contentDescription`s from `stringResource`. Decorative images use `null`. Today's `LoginScreen` logo has `"Test"`, which should be fixed.
- Touch targets are at least 48dp, and Mint text on light surfaces uses Mint dark (`#16A37A`) for contrast.
- `design:accessibility-review` can audit a finished screen.

### Text fields

The text value lives in state, and every change dispatches an action:

```kotlin
OutlinedTextField(
    value = state.plateNumber,
    onValueChange = { onAction(ManualShuttlePassAction.OnPlateNumberChange(it)) },
    isError = state.plateNumberError != null,
    supportingText = state.plateNumberError?.let { { Text(it) } },
    shape = RoundedCornerShape(12.dp),
    singleLine = true
)
```

## Legacy code / known gaps

Don't copy these patterns into new code, and don't mass-refactor them unless asked:

- `Color.kt` still has template colors (`Purple80`, …) and ad-hoc colors (`NavyBlue`, `Green`, `Orange`) that don't match `DESIGN-SYSTEM.md`. Replace them screen by screen during the redesign.
- Screens hard-code `sp`/`dp`/colors inline and mix M2 (`androidx.compose.material`) with M3 components.
- `MainActivity` wraps the whole `Navigation()` in `Box(Modifier.systemBarsPadding())`, which cancels `enableEdgeToEdge()`. `Theme.kt` colors the bars with the deprecated `accompanist-systemuicontroller`. Fix both together with the **android-edge-to-edge** skill. Until then, don't add extra system-bar padding inside screens.
- `LoginScreen` passes value lambdas (`usernameValue = { loginState.username }`) rather than plain state.

## Checklist

- [ ] The composable takes `state` and lambdas only, with no ViewModel or `NavController`.
- [ ] Colors and typography come from the theme, matching `DESIGN-SYSTEM.md` and the relevant mockup.
- [ ] It's under about 100 lines, with sub-composables extracted and shared pieces in `presentation/components`.
- [ ] `modifier: Modifier = Modifier` is applied to the root.
- [ ] Lazy items have keys.
- [ ] Loading, error and empty states are rendered.
- [ ] There are `@Preview`s in `ShuttleAppTheme` with realistic data.
- [ ] `contentDescription`s are set, and touch targets are at least 48dp.

## Related skills

- **android-presentation-mvi**: the Root/Screen split and events
- **android-state-management**: loading, error and empty logic
- **android-edge-to-edge**: insets and system bars
- **design:design-critique**, **design:accessibility-review**: reviewing a restyled screen against the mockups
