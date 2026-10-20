package com.example.shuttleapp.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.presentation.events.HomeAction
import com.example.shuttleapp.presentation.state.HomeDashboardUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme

/** How far the route card rides up into the Midnight header. */
private val HeaderOverlap = 48.dp

/** Width at which the dashboard switches to a navigation rail and two columns. */
private val WideBreakpoint = 600.dp

/** Room at the end of the list so the last row can scroll clear of the FAB. */
private val FabClearance = 88.dp

/**
 * Stateless Home dashboard. Phones get one scrolling column, a bottom bar and an Extended FAB; widths of
 * 600dp and up (tablets, landscape phones) get a navigation rail and two columns centred at
 * [Dimens.ContentMaxWidth]. The header draws behind the status bar.
 */
@Composable
fun HomeDashboard(
    ui: HomeDashboardUi,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    val onNavSelect: (HomeNavItem) -> Unit = { onAction(HomeAction.Navigate(it)) }
    val fab = @Composable { HomeFab(ui.hasActiveTrip, onClick = { onAction(HomeAction.PrimaryAction) }) }
    BoxWithConstraints(modifier.fillMaxSize().background(colors.canvas)) {
        if (maxWidth >= WideBreakpoint) {
            Row(Modifier.fillMaxSize()) {
                HomeNavRail(HomeNavItem.Home, ui.pendingSyncCount, onNavSelect)
                Box(Modifier.weight(1f).fillMaxSize()) {
                    DashboardContent(ui, wide = true, onAction, PaddingValues(0.dp))
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(Dimens.PageMargin)
                    ) { fab() }
                }
            }
        } else {
            Scaffold(
                containerColor = colors.canvas,
                contentWindowInsets = WindowInsets(0),
                floatingActionButton = fab,
                bottomBar = { HomeBottomBar(HomeNavItem.Home, ui.pendingSyncCount, onNavSelect) }
            ) { padding ->
                DashboardContent(ui, wide = false, onAction, padding)
            }
        }
    }
}

@Composable
private fun DashboardContent(
    ui: HomeDashboardUi,
    wide: Boolean,
    onAction: (HomeAction) -> Unit,
    outerPadding: PaddingValues
) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(outerPadding)) {
        // Centre the content on very wide screens while the header band still spans edge to edge.
        val side = maxOf(pageMargin(maxWidth), (maxWidth - Dimens.ContentMaxWidth) / 2)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = FabClearance)) {
            item(key = "header") {
                HomeHeader(
                    greeting = ui.greeting,
                    subtitle = ui.subtitle,
                    initials = ui.initials,
                    onOpenProfile = { onAction(HomeAction.OpenProfile) },
                    contentPadding = PaddingValues(horizontal = side),
                    bottomExtra = HeaderOverlap
                )
            }
            if (wide) {
                item(key = "columns") { WideColumns(ui, side, onAction) }
            } else {
                item(key = "route") { RouteCard(ui, Modifier.padding(horizontal = side).pullUp(HeaderOverlap)) }
                if (ui.pendingSyncCount > 0) {
                    item(key = "callout") { Callout(ui, onAction, Modifier.padding(start = side, end = side, top = Dimens.SpaceM)) }
                }
                item(key = "stats") { Stats(ui, onAction, Modifier.padding(horizontal = side, vertical = Dimens.SpaceM)) }
                item(key = "recent") { Recent(ui, onAction, Modifier.padding(horizontal = side)) }
            }
        }
    }
}

@Composable
private fun WideColumns(ui: HomeDashboardUi, side: Dp, onAction: (HomeAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = side).pullUp(HeaderOverlap),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
            RouteCard(ui)
            if (ui.pendingSyncCount > 0) Callout(ui, onAction)
            Stats(ui, onAction)
        }
        // The right column starts below the header band, where the left card overlaps it.
        Column(Modifier.weight(1f).padding(top = HeaderOverlap + Dimens.SpaceS)) {
            Recent(ui, onAction)
        }
    }
}

@Composable
private fun RouteCard(ui: HomeDashboardUi, modifier: Modifier = Modifier) =
    TodayRouteCard(ui.hasActiveTrip, ui.routeTitle, ui.departure, ui.lateCount, modifier)

@Composable
private fun Callout(ui: HomeDashboardUi, onAction: (HomeAction) -> Unit, modifier: Modifier = Modifier) =
    PendingSyncCallout(ui.pendingSyncCount, ui.isSyncing, onSyncNow = { onAction(HomeAction.SyncNow) }, modifier)

@Composable
private fun Stats(ui: HomeDashboardUi, onAction: (HomeAction) -> Unit, modifier: Modifier = Modifier) =
    DashboardStatsRow(
        passengerCount = ui.passengerCount,
        lateCount = ui.lateCount,
        pendingSyncCount = ui.pendingSyncCount,
        onOpenPassengers = if (ui.hasActiveTrip) ({ onAction(HomeAction.PrimaryAction) }) else null,
        onOpenLate = { onAction(HomeAction.OpenPassList(PassFilter.Late)) },
        onOpenNotSynced = { onAction(HomeAction.OpenPassList(PassFilter.NotSynced)) },
        modifier = modifier
    )

@Composable
private fun Recent(ui: HomeDashboardUi, onAction: (HomeAction) -> Unit, modifier: Modifier = Modifier) =
    RecentPassesSection(
        passes = ui.recentPasses,
        isLoading = ui.isLoadingPasses,
        error = ui.passesError,
        onOpenPass = { onAction(HomeAction.OpenPass(it)) },
        onViewAll = { onAction(HomeAction.OpenPassList()) },
        onRetry = { onAction(HomeAction.RetryPasses) },
        modifier = modifier
    )

private fun pageMargin(width: Dp): Dp = if (width < 360.dp) Dimens.PageMarginNarrow else Dimens.PageMargin

/** Draws the element [by] higher and gives that space back, so it overlaps whatever is above it. */
private fun Modifier.pullUp(by: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val px = by.roundToPx()
    layout(placeable.width, (placeable.height - px).coerceAtLeast(0)) { placeable.place(0, -px) }
}
