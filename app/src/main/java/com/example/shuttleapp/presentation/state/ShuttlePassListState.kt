package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.PassFilter

data class ShuttlePassListState(
    val filter: PassFilter = PassFilter.All,
    /** The search box text; matches pass codes and route names. */
    val query: String = "",
    /** Passes for the current filter and query, newest day first. */
    val sections: List<PassDaySection> = emptyList(),
    val isLoading: Boolean = true,
    val sync: SyncStatus = SyncStatus.Idle,
    val isOnline: Boolean = true,
    /** The token expired: uploads need the driver to sign in again (local data is unaffected). */
    val needsSignIn: Boolean = false,
    /** Non-null while the sign-in-again dialog is open. */
    val reauth: ReauthState? = null,
    /** Finalized passes waiting to upload, for the Not synced filter and the Sync tab badge. */
    val pendingCount: Int = 0,
    /** Late passes, for the Late filter badge. */
    val lateCount: Int = 0
)

/** One day of passes: "Today" / "Tue, Oct 20" and that day's rows. */
data class PassDaySection(
    val key: String,
    val title: String,
    val dateLabel: String,
    val passes: List<ShuttlePassItemUi>
)

/** A shuttle pass row, already formatted for display. */
data class ShuttlePassItemUi(
    val id: String,
    val code: String,
    val schedule: String,
    val routeAndTrip: String,
    val isLate: Boolean,
    val isSynced: Boolean,
    /** "Villa Estrella · In · 07:45": the row's second line. */
    val summary: String = "",
    /** When the pass was created (epoch millis); the pass list groups rows by its day. */
    val createdAt: Long = 0L
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
