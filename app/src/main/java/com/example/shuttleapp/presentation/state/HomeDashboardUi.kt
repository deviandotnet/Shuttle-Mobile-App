package com.example.shuttleapp.presentation.state

/** Everything the Home dashboard renders, already formatted for display. */
data class HomeDashboardUi(
    val greeting: String = "Hello",
    /** "Tue, Oct 20 · 2 passes today". */
    val subtitle: String = "",
    val initials: String = "",
    val hasActiveTrip: Boolean = false,
    /** "Villa Estrella · Incoming"; blank when there is no active trip. */
    val routeTitle: String = "",
    /** "16:30"; blank when the draft has no departure yet. */
    val departure: String = "",
    val passengerCount: Int = 0,
    /** Finalized passes waiting to upload: the callout, the stat tile and the Sync badge. */
    val pendingSyncCount: Int = 0,
    val isSyncing: Boolean = false,
    val lateCount: Int = 0,
    val recentPasses: List<ShuttlePassItemUi> = emptyList(),
    val isLoadingPasses: Boolean = false,
    val passesError: String? = null
)
