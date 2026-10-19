package com.example.shuttleapp.domain.model

/**
 * The signed-in driver. It survives app restarts and stays offline until an explicit logout;
 * an expired token never signs the driver out, it only blocks uploads until they sign in again.
 */
data class Session(
    val accountId: String,
    val username: String,
    val displayName: String,
    val tokenStatus: TokenStatus
)

enum class TokenStatus {
    /** The backend issued no token (current API); requests go out unauthenticated. */
    None,
    Valid,
    /** The server rejected the token (401) or it passed its expiry; sign in again to upload. */
    Expired
}

sealed interface SessionLoad {
    data object Loading : SessionLoad
    data object SignedOut : SessionLoad
    data class SignedIn(val session: Session) : SessionLoad
}

object AuthMessages {
    const val SESSION_EXPIRED = "Your sign-in has expired. Sign in again to upload; your passes are still saved on this device."
    const val SIGN_IN_NEEDS_INTERNET = "You need an internet connection to sign in."
    const val DIFFERENT_ACCOUNT = "That is a different account. Sign in as the driver who is signed in on this device."
}
