package com.example.shuttleapp.presentation.components.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.shuttleapp.presentation.state.HomeDashboardUi
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.ui.theme.ShuttleAppTheme

private val samplePasses = listOf(
    ShuttlePassItemUi("1", "SPT-0125-00001", "2025-01-02  10:45 — 11:30", "Villa Estrella · Incoming", isLate = false, isSynced = true),
    ShuttlePassItemUi("2", "SPT-0125-00002", "2025-01-02  08:20 — 09:05", "Villa Estrella · Outgoing", isLate = true, isSynced = false),
    ShuttlePassItemUi("3", "SPT-0125-00003", "2025-01-01  17:10 — 18:00", "Mamatid · Incoming", isLate = false, isSynced = false),
    ShuttlePassItemUi("4", "SPT-0125-00004", "2025-01-01  06:20 — 07:05", "Mamatid · Outgoing", isLate = false, isSynced = true),
    ShuttlePassItemUi("5", "SPT-1224-00005", "2024-12-31  17:05 — 17:55", "Villa Estrella · Incoming", isLate = false, isSynced = true)
)

private val activeTrip = HomeDashboardUi(
    greeting = "Good morning, Elmo",
    initials = "ER",
    hasActiveTrip = true,
    routeTitle = "Villa Estrella · Incoming",
    departure = "16:30",
    passengerCount = 24,
    pendingSyncCount = 3,
    lateCount = 1,
    recentPasses = samplePasses,
    totalPassCount = 12
)

private val noTrip = HomeDashboardUi(greeting = "Good evening, Elmo", initials = "ER")

@Composable
private fun Dashboard(ui: HomeDashboardUi) = ShuttleAppTheme(darkTheme = false) {
    HomeDashboard(ui, onOpenProfile = {}, onPrimaryAction = {}, onOpenPass = {}, onViewAll = {}, onNavSelect = {})
}

@Preview(name = "Phone", widthDp = 360, heightDp = 780)
@Composable
private fun PhonePreview() = Dashboard(activeTrip)

@Preview(name = "Phone large", widthDp = 412, heightDp = 915)
@Composable
private fun PhoneLargePreview() = Dashboard(activeTrip)

@Preview(name = "No active trip", widthDp = 360, heightDp = 780)
@Composable
private fun NoTripPreview() = Dashboard(noTrip)

@Preview(name = "Loading", widthDp = 360, heightDp = 780)
@Composable
private fun LoadingPreview() = Dashboard(activeTrip.copy(recentPasses = emptyList(), isLoadingPasses = true))

@Preview(name = "Error", widthDp = 360, heightDp = 780)
@Composable
private fun ErrorPreview() = Dashboard(activeTrip.copy(recentPasses = emptyList(), passesError = "Network error"))

@Preview(name = "Small phone", widthDp = 320, heightDp = 640)
@Composable
private fun SmallPhonePreview() = Dashboard(activeTrip)

@Preview(name = "Font scale 1.5", widthDp = 360, heightDp = 780, fontScale = 1.5f)
@Composable
private fun LargeFontPreview() = Dashboard(activeTrip)

@Preview(name = "Landscape phone", widthDp = 800, heightDp = 360)
@Composable
private fun LandscapePreview() = Dashboard(activeTrip)

@Preview(name = "Tablet", widthDp = 840, heightDp = 1280)
@Composable
private fun TabletPreview() = Dashboard(activeTrip)
