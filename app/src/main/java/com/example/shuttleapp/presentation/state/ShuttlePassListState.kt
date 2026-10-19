package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.PassFilter

data class ShuttlePassListState(
    val filter: PassFilter = PassFilter.All,
    val items: List<ShuttlePassItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val sync: SyncStatus = SyncStatus.Idle
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
