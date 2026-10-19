---
name: android-error-handling
description: >
  Error handling for the Shuttle app built on util/Resource (Success / Error / Loading) - wrapping
  Retrofit and Room calls, mapping exceptions and HTTP codes to user-facing messages, validation
  results from use cases, and surfacing errors in UI state. Use this skill whenever writing a
  try/catch, returning Resource from a repository, handling a failed network or database call,
  writing a validation use case, or showing an error message on screen. Trigger on phrases like
  "error handling", "Resource", "Result wrapper", "catch exception", "network error",
  "validation error", "ValidationResult", "error message", "timeout", or "server offline".
---

# Error Handling (`Resource`)

## Project context

| File | Role |
|---|---|
| `util/Resource.kt` | The **only** result wrapper: `Success(data)`, `Error(message, data?)`, `Loading(isLoading)` |
| `domain/usecase/AuthValidationResult.kt`, `ShuttlePassValidationResult.kt` | Validation outcome `(successful: Boolean, errorMessage: String?)` |
| `data/repositoryImpl/*` | Where exceptions are caught and converted to `Resource.Error` |

```kotlin
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(val isLoading: Boolean = true) : Resource<T>(null)
}
```

## Rules

- Use `Resource` for every network and fallible database operation (`AGENTS.md`). Do not introduce Kotlin `Result`, `Either` or a second sealed wrapper.
- **Catch exceptions in the data layer**, in the repository implementation that makes the call. ViewModels receive a `Resource`, not a raw exception.
- **No empty `catch` blocks** and no `catch` that only calls `printStackTrace()`. Every catch either returns or emits `Resource.Error` or logs and rethrows.
- **Never swallow `CancellationException`.** Rethrow it, or catch narrower types (`IOException`, `HttpException`) before a general `Exception`.
- Error messages are plain user-facing `String`s. Keep the wording consistent with the messages that already exist, listed below.
- Validation is not an exception. Validators return an `XValidationResult`.

## Patterns

### Which shape to return

| Situation | Signature | Example |
|---|---|---|
| Network call whose UI shows a spinner | `fun x(...): Flow<Resource<T>>` emitting `Loading` → `Success`/`Error` | `AuthRepository.postLogin` |
| One-shot call driven by the ViewModel's own loading flag (sync, upload) | `suspend fun x(...): Resource<T>` | `ShuttlePassRepository.postShuttlePassWithPassenger` |
| Observing Room | `fun x(...): Flow<List<T>>` (Room errors are rare. Add `.catch {}` only where a failure is meaningful.) | `getAllShuttlePassWithPassengers` |
| Local write that can fail (constraint, disk) | `suspend fun x(...): Resource<Unit>` | inserting a passenger with a foreign key |

### Mapping exceptions and HTTP responses (data layer)

Use one helper so the messages stay consistent. Put it in `data/network/SafeApiCall.kt` the first time it's needed:

```kotlin
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Resource<T> = try {
    val response = call()
    if (response.isSuccessful) Resource.Success(response.body())
    else Resource.Error(httpErrorMessage(response.code()))
} catch (e: CancellationException) {
    throw e
} catch (e: SocketTimeoutException) {
    Resource.Error("The server may be offline or experiencing downtime. Please try again later.")
} catch (e: IOException) {          // ConnectException, UnknownHostException, ...
    Resource.Error("Cannot connect to server. Please check your connection and try again.")
} catch (e: Exception) {
    Log.e("safeApiCall", "Unexpected error", e)
    Resource.Error("Something went wrong. Please try again later.")
}

private fun httpErrorMessage(code: Int) = when (code) {
    400 -> "The request was invalid."
    401, 403 -> "Your session has expired. Please sign in again."
    404 -> "The requested record was not found."
    in 500..599 -> "Server error. Please try again later."
    else -> "Request failed ($code)."
}
```

Wrapped in a `Flow` for screens that show loading:

```kotlin
override fun postLogin(credentials: LoginCredentials): Flow<Resource<UserData>> = flow {
    emit(Resource.Loading())
    when (val result = safeApiCall { authApi.postLogin(credentials.toDto()) }) {
        is Resource.Success -> {
            val body = result.data
            if (body?.message == "login successful") emit(Resource.Success(body.userData?.toDomain()))
            else emit(Resource.Error("Login failed. ${body?.message.orEmpty()}"))
        }
        is Resource.Error -> emit(Resource.Error(result.message ?: "Login failed."))
        is Resource.Loading -> Unit
    }
}.flowOn(Dispatchers.IO)
```

The backend can return HTTP 200 with a failure inside the body (`message != "login successful"`, `status == 400` on register). Always check the body as well as `isSuccessful`.

### Consuming in the ViewModel

```kotlin
authRepository.postLogin(credentials).collect { result ->
    when (result) {
        is Resource.Loading -> _state.update { it.copy(isLoading = true, errorMessage = null) }
        is Resource.Success -> {
            _state.update { it.copy(isLoading = false) }
            _events.send(LoginEvent.NavigateToHome(result.data!!.accountId))
        }
        is Resource.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
    }
}
```

### Validation use cases (domain)

```kotlin
class ValidatePlateNumberInput {
    fun execute(plateNumber: String, plateNumbers: List<String>): ShuttlePassValidationResult = when {
        plateNumber.isBlank() -> ShuttlePassValidationResult(false, "Plate number can't be blank")
        plateNumbers.none { it.equals(plateNumber, ignoreCase = true) } ->
            ShuttlePassValidationResult(false, "Plate Number does not exist")
        else -> ShuttlePassValidationResult(true)
    }
}
```

The ViewModel runs all validators, writes each `errorMessage` into the matching state field, and only proceeds when every result is `successful`.

## Legacy code / known gaps

Don't copy these patterns into new code, and don't mass-refactor them unless asked:

- `AuthRepositoryImpl.postRegister` emits nothing when `!response.isSuccessful`, so the UI stays loading forever.
- `RoutesRepositoryImpl` imports `coil.network.HttpException` and `okio.IOException` by mistake. Use `retrofit2.HttpException` and `java.io.IOException`.
- ViewModels such as `LoginViewModel` wrap repository calls in `try { } catch (ex: Exception) { ex.printStackTrace() }`. That is redundant once the repository returns `Resource`, and it hides bugs.
- `LoginState.loginErrorMessage` is a `var` mutated in place (`resetLoginValidationMessage`). State must be immutable and updated with `_state.update { }`.
- Typed errors (a `DataError` enum plus a `UiText` mapping to `strings.xml`) are a possible future upgrade for localization. They are not the current convention, so don't start using them in one corner of the app.

## Checklist

- [ ] The repository returns `Resource` or `Flow<Resource<T>>`, and the call is wrapped (`safeApiCall` or try/catch) in the data layer.
- [ ] `CancellationException` is rethrown, and there is no empty or print-only catch.
- [ ] The response body's own success flag is checked, not just the HTTP status.
- [ ] Every branch, including the `!isSuccessful` one, emits or returns something.
- [ ] The ViewModel clears the error when starting, and sets `isLoading = false` in both success and error.
- [ ] Validators return an `XValidationResult`, with no exceptions for invalid input.

## Related skills

- **android-data-layer**: where these calls live
- **android-state-management**: loading, error and empty UI states
- **android-presentation-mvi**: showing errors in state vs one-time events
