package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.PassFilter

data class ShuttlePassListState(
    val filter: PassFilter = PassFilter.All,
    val items: List<ShuttlePassItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val sync: SyncStatus = SyncStatus.Idle,
    val isOnline: Boolean = true,
    /** The token expired: uploads need the driver to sign in again (local data is unaffected). */
    val needsSignIn: Boolean = false,
    /** Non-null while the sign-in-again dialog is open. */
    val reauth: ReauthState? = null,
    /** Finalized passes waiting to upload, for the Sync tab badge. */
    val pendingCount: Int = 0
)

/** A shuttle pass row, already formatted for display. */
data class ShuttlePassItemUi(
    val id: String,
    val code: String,
    val schedule: String,
    val routeAndTrip: String,
    val isLate: Boolean,
    val isSynced: Boolean
)

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Running : SyncStatus
    data class Success(val message: String) : SyncStatus
    data class Failed(val message: String) : SyncStatus
}

data class ReauthState(
    val username: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)

/** Progress and preconditions of an upload of all pending passes, shared by every screen that offers "Sync now". */
data class PassUploadState(
    val sync: SyncStatus = SyncStatus.Idle,
    val isOnline: Boolean = true,
    /** The token expired: uploads need the driver to sign in again (local data is unaffected). */
    val needsSignIn: Boolean = false,
    /** Non-null while the sign-in-again dialog is open. */
    val reauth: ReauthState? = null
)
