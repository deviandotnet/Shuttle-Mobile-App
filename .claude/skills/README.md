# Project skills

Claude Code loads every folder here that contains a `SKILL.md`. A skill is pulled in automatically when a request matches its `description`, or explicitly with `/<skill-name>`.

**Precedence:** `AGENTS.md` and `.cursorrules` are binding. Skills explain *how* to apply those rules in this codebase. If a skill ever contradicts them, `AGENTS.md` wins, and the skill should be fixed.

## Project-adapted skills

These are written for this app's actual stack: a single `:app` module, Hilt, Retrofit + Gson, Room, `util/Resource`, Navigation-Compose 2.9 and JUnit4.

| Skill | Use it for |
|---|---|
| [`shuttle-add-screen`](shuttle-add-screen/SKILL.md) | **Start here** for any new or rebuilt screen. It's the end-to-end workflow linking all the skills below. |
| [`android-project-structure`](android-project-structure/SKILL.md) | Where a file goes, layer import rules, Gradle and version catalog |
| [`android-di-hilt`](android-di-hilt/SKILL.md) | `AppModule` / `RepositoryModule` / `UseCaseModule`, `@HiltViewModel`, `hiltViewModel()` scoping |
| [`android-data-layer`](android-data-layer/SKILL.md) | Retrofit APIs, DTOs, Room entities and DAOs, mappers, `XRepositoryImpl`, the shuttle-pass sync lifecycle |
| [`android-error-handling`](android-error-handling/SKILL.md) | `Resource`, `safeApiCall`, error messages, validation results |
| [`android-presentation-mvi`](android-presentation-mvi/SKILL.md) | `XState` / `XAction` / `XEvent`, ViewModel, `XRoot` / `XScreen` |
| [`android-state-management`](android-state-management/SKILL.md) | StateFlow, Channel events, coroutines and dispatchers, loading, error and empty states |
| [`android-navigation`](android-navigation/SKILL.md) | `Navigation.kt`, type-safe routes, arguments, back stack |
| [`android-compose-ui`](android-compose-ui/SKILL.md) | Composables, redesign tokens (`DESIGN-SYSTEM.md`), previews, accessibility, performance |
| [`android-testing`](android-testing/SKILL.md) | JUnit4 + Turbine + coroutines-test, fakes, DAO and Compose UI tests |

## Upstream Google skills

These come from Google. Each starts with a short **"In this project"** section (our notes), followed by the upstream text **verbatim**. To update one from Google, replace only the text below that section's closing line.

| Skill | Relevance here |
|---|---|
| [`r8-analyzer`](r8-analyzer/SKILL.md) | **Relevant: release risk.** Release is minified, but `proguard-rules.pro` keeps the wrong package (`com.example.shuttle…`), so Gson DTOs are unprotected. |
| [`agp-9-upgrade`](agp-9-upgrade/SKILL.md) | **Relevant: migration incomplete.** AGP 9.1.1 runs in compatibility mode (`builtInKotlin=false`, `newDsl=false`, …). KSP and Hilt are below the versions it requires. |
| [`edge-to-edge`](edge-to-edge/SKILL.md) | **Relevant.** `enableEdgeToEdge()` is called, but a root `systemBarsPadding()` in `MainActivity` cancels it. `accompanist-systemuicontroller` should be removed. |
| [`navigation-3`](navigation-3/SKILL.md) | Nav3 isn't used (the app is on Navigation 2.9). Its [`type-safe-destinations.md`](navigation-3/references/android/guide/navigation/type-safe-destinations.md) reference *is* the guide for our Nav2 typed-route migration. |
| [`migrate-xml-views-to-jetpack-compose`](migrate-xml-views-to-jetpack-compose/SKILL.md) | Not applicable: there are no XML layouts and `AGENTS.md` forbids them. Kept for reference only. |

The upstream frontmatter says "Complete terms in LICENSE.txt", but the `LICENSE.txt` files weren't included when these were copied in. Restore them from the Google Android skills source they were downloaded from before redistributing this repo.

## Known issues found while auditing the skills

These are **app fixes that haven't been made yet**. The skills describe them, but no code has changed.

1. `app/proguard-rules.pro` targets `com.example.shuttle.data.network.**`, but the real package is `com.example.shuttleapp`. Release builds can break JSON parsing. See **r8-analyzer** and **android-data-layer**.
2. The AGP 9 migration isn't finished: there are compatibility flags in `gradle.properties`, KSP is 2.3.2 (needs ≥ 2.3.6), Hilt is 2.57.2 (needs ≥ 2.59.2), and the `kotlin-android` plugin and `kotlinOptions` are still in use. See **agp-9-upgrade**.
3. `MainActivity` wraps the whole app in `systemBarsPadding()`, which defeats edge-to-edge. See **edge-to-edge**.

## Writing or editing a project skill

Use the same layout for every project-adapted `SKILL.md`:

1. **Frontmatter:**
   - `name`, which must equal the folder name;
   - `description`, which says what the skill covers, when to use it, and the trigger phrases.
2. **Project context:** the real files and paths involved.
3. **Rules:** the binding rules, traced to `AGENTS.md` / `.cursorrules`.
4. **Patterns:** short examples using real shuttle types (`ShuttlePass`, `LoginViewModel`, …).
5. **Legacy code / known gaps:** existing code not to copy, plus "don't mass-refactor unless asked".
6. **Checklist**
7. **Related skills**

Keep examples consistent with the other skills. Events use `Channel`, results use `Resource`, and IO lives in repositories. Don't reintroduce libraries the project doesn't use (Koin, Ktor, JUnit5, multi-module/KMP).
