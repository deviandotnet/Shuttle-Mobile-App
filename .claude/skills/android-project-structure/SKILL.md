---
name: android-project-structure
description: >
  Package layout, layer dependency rules and Gradle conventions for the single-module Shuttle app
  (:app, package com.example.shuttleapp). Use this skill whenever deciding where a new class or
  file should live, adding a package, checking which layer may import which, adding a dependency
  to Gradle, or answering "how should I structure this". Trigger on phrases like "where does X go",
  "project structure", "which package", "add a dependency", "version catalog", "layer rules",
  "clean architecture", or "can presentation import data".
---

# Project Structure (single `:app` module)

## Project context

This is one Gradle module, `:app`, using the Kotlin DSL. The version catalog is `gradle/libs.versions.toml`. Layers are separated by **package**, not by module. Source root: `app/src/main/java/com/example/shuttleapp/`.

```
com.example.shuttleapp
├── ShuttleApp.kt / MainActivity.kt / Navigation.kt
├── config/          API.kt (BASE_URL), Database.kt (ShuttleDatabase)
├── data/
│   ├── local/       Room entities (XEntity) + dao/ (XDao)
│   ├── mappers/     extension functions: toEntity / toDomain / toDto
│   ├── network/     Retrofit APIs (AuthApi, ShuttleApi), request/, response/ DTOs
│   └── repositoryImpl/  XRepositoryImpl
├── di/              AppModule, RepositoryModule, UseCaseModule
├── domain/
│   ├── model/       pure Kotlin domain models
│   ├── repository/  XRepository interfaces
│   └── usecase/<feature>/  use cases + validators, XValidationResult
├── presentation/
│   ├── <Screen>Screen.kt   XRoot + XScreen composables
│   ├── components/  reusable composables
│   ├── events/      XAction (user intents), XEvent (one-time effects)
│   ├── state/       XState data classes, XUi models
│   └── viewmodel/   XViewModel
├── ui/theme/        Color.kt, Type.kt, Theme.kt
└── util/            Resource, DateTime, StringExtension, IsOnlineCapable
```

Tests: JVM unit tests are in `app/src/test/java/com/example/shuttleapp/` and instrumented tests are in `app/src/androidTest/java/com/example/shuttleapp/`. Mirror the main package path.

## Rules

Layer dependencies (Clean Architecture, from `AGENTS.md`):

| Layer | May import | Must not import |
|---|---|---|
| `presentation` | `domain`, `ui.theme`, `util`, Android/Compose | anything in `data` (entities, DTOs, DAOs, APIs, mappers) |
| `domain` | Kotlin stdlib, coroutines `Flow`, `javax.inject`, `util.Resource` | Android framework, Room, Retrofit, `data`, `presentation` |
| `data` | `domain`, `config`, `util`, Room, Retrofit | `presentation` |
| `di` | everything (it wires the graph) | — |

- Keep code in the package of the layer that owns it. Shared composables go in `presentation/components`, and shared non-UI helpers go in `util`.
- Do not create new Gradle modules, `core`/`feature` modules or convention plugins. Modularization is out of scope for this app.
- The build is on AGP 9.1.1 but still in compatibility mode: `gradle.properties` sets `android.builtInKotlin=false`, `android.newDsl=false` and others. Don't add new uses of the legacy DSL. Finishing the migration is covered by the **android-agp-9-upgrade** skill.
- Put new dependency versions in `gradle/libs.versions.toml` and reference them as `libs.*`. Many dependencies in `app/build.gradle.kts` are still hard-coded strings. Migrate one only when you touch it.
- New Room entities and DAOs are registered in `config/Database.kt`. New endpoints go on `AuthApi`/`ShuttleApi` or a new API provided in `di/AppModule.kt`.

## Where does it go?

| I'm adding… | Location |
|---|---|
| A business model (e.g. `Manifest`) | `domain/model/Manifest.kt` |
| A repository contract | `domain/repository/ManifestRepository.kt` |
| Its implementation | `data/repositoryImpl/ManifestRepositoryImpl.kt` + a binding in `di/RepositoryModule.kt` |
| A validation or business rule | `domain/usecase/<feature>/ValidateXInput.kt` + a `@Provides` in `di/UseCaseModule.kt` |
| A JSON request/response body | `data/network/request/XRequestDto.kt` or `data/network/response/XDto.kt` |
| A Room table | `data/local/XEntity.kt`, `data/local/dao/XDao.kt`, registered in `config/Database.kt` |
| A conversion between types | `data/mappers/XMapper.kt` |
| A screen | `presentation/XScreen.kt`, `presentation/state/XState.kt`, `presentation/events/XAction.kt` + `XEvent.kt`, `presentation/viewmodel/XViewModel.kt`, a route in `Navigation.kt` |
| A reusable UI piece | `presentation/components/` |
| A color or text style | `ui/theme/` (follow `.claude/shuttle-mobile-redesign-v2/DESIGN-SYSTEM.md`) |

## Legacy code / known gaps

Don't copy these patterns into new code, and don't mass-refactor them unless asked:

- Domain repository interfaces (`ShuttlePassRepository`, `AuthRepository`) import `data` entities and DTOs. New interfaces take and return `domain/model` types only.
- Several screens and ViewModels import `com.example.shuttleapp.data.*` directly (e.g. `LoginScreen` builds a `LoginRequestBodyDto`).
- `presentation/AVCurrentPassScreenTest.kt` is a production screen with a misleading name. It is not a test.

## Checklist

- [ ] The new file is in the package of the layer that owns it (see the table above).
- [ ] No `com.example.shuttleapp.data` import in `presentation` or `domain`.
- [ ] No `android.*` import in `domain`.
- [ ] The DI binding was added (see **android-di-hilt**).
- [ ] Any new dependency is declared in `libs.versions.toml`.

## Related skills

- **android-di-hilt**: wiring new classes
- **android-data-layer**, **android-presentation-mvi**: what goes inside each layer
- **android-shuttle-add-screen**: the full workflow for a new screen
