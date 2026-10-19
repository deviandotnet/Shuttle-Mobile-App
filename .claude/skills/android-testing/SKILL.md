---
name: android-testing
description: >
  Testing for the Shuttle app - JUnit4 + kotlinx-coroutines-test + Turbine for ViewModel and use
  case unit tests, a MainDispatcherRule, hand-written fakes of domain repository interfaces,
  in-memory Room DAO tests, Compose UI tests with createComposeRule and the Robot pattern, and the
  Gradle commands to run them. Use this skill whenever writing, reviewing or running tests, or
  setting up test dependencies. Trigger on phrases like "write a test", "unit test",
  "test the ViewModel", "fake repository", "Turbine", "runTest", "DAO test", "Compose UI test",
  "ComposeTestRule", "test coverage", or "testDebugUnitTest".
---

# Testing

## Project context

- Unit tests (JVM) are in `app/src/test/java/com/example/shuttleapp/`. Instrumented tests (device or emulator) are in `app/src/androidTest/java/com/example/shuttleapp/`. Mirror the main package (`…/presentation/viewmodel/LoginViewModelTest.kt`).
- Only the template `ExampleUnitTest`/`ExampleInstrumentedTest` exist, so there is no real suite yet.
- Already declared: `junit:4.13.2`, `androidx.test.ext:junit`, `espresso-core` and Compose `ui-test-junit4`/`ui-test-manifest`.
- Commands:

```bash
./gradlew testDebugUnitTest
./gradlew testDebugUnitTest --tests "com.example.shuttleapp.presentation.viewmodel.LoginViewModelTest"
./gradlew connectedDebugAndroidTest
```

If `./gradlew` can't find Java, set `JAVA_HOME` to Android Studio's bundled JDK (`…/Android Studio/jbr`).

## Rules

- **Stack:** JUnit4, matching the existing dependencies and Compose's `createComposeRule`, plus `kotlinx-coroutines-test` and Turbine. Use plain `org.junit.Assert` assertions (`assertEquals`, `assertTrue`). Don't introduce JUnit5, MockK or AssertK unless asked.
- **Fakes over mocks.** Write small in-memory implementations of the `domain/repository` interfaces in `app/src/test/.../fake/`.
- Construct ViewModels **directly** with fakes. Hilt isn't needed in unit tests.
- Test **behaviour through the public API**: `onAction(...)` in, `state` and `events` out.
- Every bug fix in the sync, draft or finalize logic gets a regression test.

### Dependencies to add (first test PR)

```toml
# gradle/libs.versions.toml
[versions]
coroutinesTest = "1.10.2"
turbine = "1.2.0"

[libraries]
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
```

```kotlin
// app/build.gradle.kts
testImplementation(libs.kotlinx.coroutines.test)
testImplementation(libs.turbine)
androidTestImplementation(libs.kotlinx.coroutines.test)
```

## Patterns

### `MainDispatcherRule` (`app/src/test/.../MainDispatcherRule.kt`)

```kotlin
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

### Fake repository

```kotlin
class FakeAuthRepository : AuthRepository {
    var loginResult: Resource<UserData> = Resource.Success(testUser)
    override fun postLogin(credentials: LoginCredentials): Flow<Resource<UserData>> = flow {
        emit(Resource.Loading())
        emit(loginResult)
    }
}

class FakeShuttlePassRepository : ShuttlePassRepository {
    private val passes = MutableStateFlow<List<ShuttlePass>>(emptyList())
    var uploadResult: Resource<Unit> = Resource.Success(Unit)
    override fun observeShuttlePasses(driverId: String) = passes.map { list -> list.filter { it.driver == driverId } }
    // ...
}
```

### ViewModel test

```kotlin
class LoginViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val viewModel = LoginViewModel(authRepository, ValidateLoginInput())

    @Test
    fun `blank credentials show validation error and do not call login`() = runTest {
        viewModel.onAction(LoginAction.OnLoginClick)
        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `successful login navigates home`() = runTest {
        viewModel.onAction(LoginAction.OnUsernameChange("jdelacruz"))
        viewModel.onAction(LoginAction.OnPasswordChange("secret"))
        viewModel.events.test {
            viewModel.onAction(LoginAction.OnLoginClick)
            assertEquals(LoginEvent.NavigateToHome(testUser.accountId), awaitItem())
        }
    }

    @Test
    fun `server error is shown in state`() = runTest {
        authRepository.loginResult = Resource.Error("Connection failed. Cannot connect to server.")
        viewModel.state.test {
            skipItems(1) // initial
            viewModel.onAction(LoginAction.OnUsernameChange("a")); viewModel.onAction(LoginAction.OnPasswordChange("b"))
            viewModel.onAction(LoginAction.OnLoginClick)
            assertEquals("Connection failed. Cannot connect to server.", expectMostRecentItem().errorMessage)
        }
    }
}
```

### Validation use case test

These are pure Kotlin and quick to write, so they make the best first tests:

```kotlin
class ValidatePlateNumberInputTest {
    private val validate = ValidatePlateNumberInput()
    private val known = listOf("ABC 1234")
    @Test fun `blank plate number fails`() = assertFalse(validate.execute("", known).successful)
    @Test fun `unknown plate number fails`() = assertFalse(validate.execute("ZZZ 0000", known).successful)
    @Test fun `known plate number passes case-insensitively`() = assertTrue(validate.execute("abc 1234", known).successful)
}
```

### Room DAO test (instrumented, `app/src/androidTest/...`)

```kotlin
@RunWith(AndroidJUnit4::class)
class ShuttlePassDaoTest {
    private lateinit var db: ShuttleDatabase
    private lateinit var dao: ShuttlePassDao

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ShuttleDatabase::class.java)
            .allowMainThreadQueries().build()
        dao = db.ShuttlePassDao()
    }
    @After fun tearDown() = db.close()

    @Test fun finalizedUnsyncedPass_isReturnedForSync() = runTest { /* insert route/shuttle parents, then the pass */ }
}
```

`ShuttlePassEntity` has foreign keys to `RouteEntity` and `ShuttleEntity`, so insert the parent rows first.

### Compose UI test and Robot pattern

Test the stateless `XScreen` with a fixed state:

```kotlin
class LoginScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test fun showsErrorMessage() {
        composeTestRule.setContent {
            ShuttleAppTheme { LoginScreen(state = LoginState(errorMessage = "Login failed."), onAction = {}) }
        }
        composeTestRule.onNodeWithText("Login failed.").assertIsDisplayed()
    }
}
```

Once a screen has 3+ UI tests or a multi-step flow (fill the manual pass form → submit → confirm), move the interactions into a `XRobot(composeTestRule)` whose functions `apply { … }` and chain, so tests read like `robot.enterPlate("ABC 1234").submit().assertConfirmationShown()`.

## What to test first

1. Validators in `domain/usecase/**`: pure, numerous and cheap.
2. The shuttle-pass lifecycle: draft → finalize → unsynced query → upload marks `isSynced` → purge after one month. Cover it in `ShuttlePassDao` and `ShuttlePassRepositoryImpl`, where the data-loss risk is.
3. `LoginViewModel` and any ViewModel being refactored to MVI. Write the tests **before** the refactor so they guard the behaviour.
4. Mappers between DTOs and entities, especially sync DTO field names.

## Legacy code / known gaps

Don't copy these patterns into new code, and don't mass-refactor them unless asked:

- Repositories take `ShuttleDatabase` directly, so repository unit tests need an in-memory DB (instrumented) or a refactor to inject DAOs.
- `ShuttleViewModel` is too large to test well as-is. Test the per-screen ViewModels split out of it instead.
- Some interfaces return `Flow` from a `suspend fun`. Fakes must match the signature, so check the interface before writing one.

## Checklist

- [ ] The test is in the mirrored package under `src/test` (JVM) or `src/androidTest` (needs Android or Room).
- [ ] `MainDispatcherRule` is used for anything touching `viewModelScope`.
- [ ] Dependencies are fakes of domain interfaces, with no Hilt and no network.
- [ ] Assertions cover state **and** events, plus error and empty paths.
- [ ] `./gradlew testDebugUnitTest` passes locally.

## Related skills

- **android-presentation-mvi**: what a testable ViewModel looks like
- **android-data-layer**: the lifecycle under test
- **android-error-handling**: `Resource` results that fakes return
