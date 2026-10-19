package com.example.shuttleapp.presentation.state

/** Everything the Home dashboard renders, already formatted for display. */
data class HomeDashboardUi(
    val greeting: String = "Hello",
    val initials: String = "",
    val hasActiveTrip: Boolean = false,
    /** "Villa Estrella · Incoming"; blank when there is no active trip. */
    val routeTitle: String = "",
    /** "16:30"; blank when the draft has no departure yet. */
    val departure: String = "",
    val passengerCount: Int = 0,
    val pendingSyncCount: Int = 0,
    val lateCount: Int = 0,
    val recentPasses: List<ShuttlePassItemUi> = emptyList(),
    /** All completed passes, for the "View all (N)" link. */
    val totalPassCount: Int = 0,
    val isLoadingPasses: Boolean = false,
    val passesError: String? = null
) {
    val activePassCount: Int get() = if (hasActiveTrip) 1 else 0
}
