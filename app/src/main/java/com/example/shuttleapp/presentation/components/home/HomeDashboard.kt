package com.example.shuttleapp.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.example.shuttleapp.presentation.state.HomeDashboardUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme

/** How far the route card rides up into the Midnight header. */
private val HeaderOverlap = 48.dp

/** Width at which the dashboard switches to a navigation rail and two columns. */
private val WideBreakpoint = 600.dp

/** Widest the pinned action button grows on wide layouts. */
private val WideActionMaxWidth = 420.dp

/**
 * Stateless Home dashboard. Phones get one scrolling column and a bottom bar; widths of 600dp and up
 * (tablets, landscape phones) get a navigation rail and two columns centred at [Dimens.ContentMaxWidth].
 * Either way the primary action is pinned at the bottom so it stays reachable as the list grows.
 */
@Composable
fun HomeDashboard(
    ui: HomeDashboardUi,
    onOpenProfile: () -> Unit,
    onPrimaryAction: () -> Unit,
    onOpenPass: (String) -> Unit,
    onViewAll: () -> Unit,
    onNavSelect: (HomeNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    BoxWithConstraints(modifier.fillMaxSize().background(colors.canvas)) {
        if (maxWidth >= WideBreakpoint) {
            Row(Modifier.fillMaxSize()) {
                HomeNavRail(HomeNavItem.Home, onNavSelect)
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    DashboardContent(ui, wide = true, onOpenProfile, onOpenPass, onViewAll, PaddingValues(0.dp), Modifier.weight(1f))
                    StickyActionBar(
                        hasActiveTrip = ui.hasActiveTrip,
                        onClick = onPrimaryAction,
                        horizontalPadding = Dimens.PageMargin,
                        modifier = Modifier.navigationBarsPadding(),
                        maxButtonWidth = WideActionMaxWidth
                    )
                }
            }
        } else {
            val barMargin = pageMargin(maxWidth)
            Scaffold(
                containerColor = colors.canvas,
                contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    Column {
                        StickyActionBar(ui.hasActiveTrip, onPrimaryAction, horizontalPadding = barMargin)
                        HomeBottomBar(HomeNavItem.Home, onNavSelect)
                    }
                }
            ) { padding ->
                DashboardContent(ui, wide = false, onOpenProfile, onOpenPass, onViewAll, padding)
            }
        }
    }
}

@Composable
private fun DashboardContent(
    ui: HomeDashboardUi,
    wide: Boolean,
    onOpenProfile: () -> Unit,
    onOpenPass: (String) -> Unit,
    onViewAll: () -> Unit,
    outerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.fillMaxSize().padding(outerPadding)) {
        // Centre the content on very wide screens while the header band still spans edge to edge.
        val side = maxOf(pageMargin(maxWidth), (maxWidth - Dimens.ContentMaxWidth) / 2)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Dimens.SpaceL)) {
            item(key = "header") {
                HomeHeader(ui.greeting, ui.initials, onOpenProfile, PaddingValues(horizontal = side), bottomExtra = HeaderOverlap)
            }
            if (wide) {
                item(key = "columns") { WideColumns(ui, side, onOpenPass, onViewAll) }
            } else {
                item(key = "route") { RouteCard(ui, Modifier.padding(horizontal = side).pullUp(HeaderOverlap)) }
                item(key = "stats") { Stats(ui, Modifier.padding(horizontal = side, vertical = Dimens.SpaceM)) }
                item(key = "recent") { Recent(ui, onOpenPass, onViewAll, Modifier.padding(horizontal = side)) }
            }
        }
    }
}

@Composable
private fun WideColumns(
    ui: HomeDashboardUi,
    side: Dp,
    onOpenPass: (String) -> Unit,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = side).pullUp(HeaderOverlap),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
            RouteCard(ui)
            Stats(ui)
        }
        // The right column starts below the header band, where the left card overlaps it.
        Column(Modifier.weight(1f).padding(top = HeaderOverlap + Dimens.SpaceS)) {
            Recent(ui, onOpenPass, onViewAll)
        }
    }
}

@Composable
private fun RouteCard(ui: HomeDashboardUi, modifier: Modifier = Modifier) =
    TodayRouteCard(ui.hasActiveTrip, ui.routeTitle, ui.departure, ui.lateCount, modifier)

@Composable
private fun Stats(ui: HomeDashboardUi, modifier: Modifier = Modifier) =
    DashboardStatsRow(ui.passengerCount, ui.activePassCount, ui.pendingSyncCount, modifier)

@Composable
private fun Recent(ui: HomeDashboardUi, onOpenPass: (String) -> Unit, onViewAll: () -> Unit, modifier: Modifier = Modifier) =
    RecentPassesSection(ui.recentPasses, ui.totalPassCount, ui.isLoadingPasses, ui.passesError, onOpenPass, onViewAll, modifier)

private fun pageMargin(width: Dp): Dp = if (width < 360.dp) Dimens.PageMarginNarrow else Dimens.PageMargin

/** Draws the element [by] higher and gives that space back, so it overlaps whatever is above it. */
private fun Modifier.pullUp(by: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val px = by.roundToPx()
    layout(placeable.width, (placeable.height - px).coerceAtLeast(0)) { placeable.place(0, -px) }
}
