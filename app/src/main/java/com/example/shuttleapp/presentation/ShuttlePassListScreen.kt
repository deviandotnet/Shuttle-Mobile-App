package com.example.shuttleapp.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.presentation.components.ActionDropdownMenu
import com.example.shuttleapp.presentation.components.ConnectionBanner
import com.example.shuttleapp.presentation.components.ObserveAsEvents
import com.example.shuttleapp.presentation.components.PassFilterTabs
import com.example.shuttleapp.presentation.components.PassRow
import com.example.shuttleapp.presentation.components.PassSearchField
import com.example.shuttleapp.presentation.components.StyledSnackBarHost
import com.example.shuttleapp.presentation.components.UploadDialogs
import com.example.shuttleapp.presentation.components.home.HomeBottomBar
import com.example.shuttleapp.presentation.components.home.HomeNavItem
import com.example.shuttleapp.presentation.components.home.MapLines
import com.example.shuttleapp.presentation.components.home.TabTopBar
import com.example.shuttleapp.presentation.events.ShuttlePassListAction
import com.example.shuttleapp.presentation.events.ShuttlePassListEvent
import com.example.shuttleapp.presentation.state.PassDaySection
import com.example.shuttleapp.presentation.state.PassUploadState
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.presentation.state.ShuttlePassListState
import com.example.shuttleapp.presentation.state.SyncStatus
import com.example.shuttleapp.presentation.viewmodel.ShuttlePassListViewModel
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType
import kotlinx.coroutines.launch

@Composable
fun ShuttlePassListRoot(
    viewModel: ShuttlePassListViewModel,
    onBack: () -> Unit,
    onOpenPass: (id: String) -> Unit,
    onNavigate: (HomeNavItem) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ShuttlePassListEvent.NavigateToPass -> onOpenPass(event.id)
            ShuttlePassListEvent.NavigateBack -> onBack()
        }
    }
    ShuttlePassListScreen(state = state, onAction = viewModel::onAction, onNavigate = onNavigate)
}

/** Passes tab: every finished pass by day, searchable and filterable by Late / Not synced (Home's stat tiles open it pre-filtered). */
@Composable
fun ShuttlePassListScreen(
    state: ShuttlePassListState,
    onAction: (ShuttlePassListAction) -> Unit,
    onNavigate: (HomeNavItem) -> Unit
) {
    val colors = ShuttleTheme.colors
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = colors.canvas,
        snackbarHost = { StyledSnackBarHost(hostState = snackBarHostState) },
        bottomBar = { HomeBottomBar(HomeNavItem.Passes, state.pendingCount, onNavigate) },
        topBar = {
            PassListHeader(
                query = state.query,
                onQueryChange = { onAction(ShuttlePassListAction.QueryChange(it)) },
                onUploadAll = { onAction(ShuttlePassListAction.UploadAll) },
                onDeleteAll = {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(
                            message = "Delete not authorized",
                            actionLabel = "Dismiss",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            ConnectionBanner(
                isOnline = state.isOnline,
                needsSignIn = state.needsSignIn,
                onSignIn = { onAction(ShuttlePassListAction.ShowReauth) }
            )
            PassFilterTabs(
                selected = state.filter,
                lateCount = state.lateCount,
                notSyncedCount = state.pendingCount,
                onSelect = { onAction(ShuttlePassListAction.SelectFilter(it)) },
                modifier = Modifier.padding(start = Dimens.PageMarginNarrow, end = Dimens.PageMarginNarrow, top = Dimens.SpaceM)
            )
            PassListContent(state, onAction)
        }
        UploadDialogs(
            upload = PassUploadState(state.sync, state.isOnline, state.needsSignIn, state.reauth),
            onDismissResult = { onAction(ShuttlePassListAction.DismissSyncResult) },
            onSubmitReauth = { username, password -> onAction(ShuttlePassListAction.SubmitReauth(username, password)) },
            onDismissReauth = { onAction(ShuttlePassListAction.DismissReauth) }
        )
    }
}

/** Midnight band: the "Passes" app bar with its ⋮ menu, and the search box under it. */
@Composable
private fun PassListHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onUploadAll: () -> Unit,
    onDeleteAll: () -> Unit
) {
    val colors = ShuttleTheme.colors
    Box(Modifier.fillMaxWidth().background(colors.brandBand)) {
        MapLines(Modifier.matchParentSize())
        Column {
            TabTopBar(
                title = "Passes",
                containerColor = Color.Transparent,
                actions = { ActionDropdownMenu(onUploadAll = onUploadAll, onDeleteAll = onDeleteAll) }
            )
            PassSearchField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.padding(start = Dimens.PageMarginNarrow, end = Dimens.PageMarginNarrow, bottom = Dimens.SpaceM)
            )
        }
    }
}

@Composable
private fun PassListContent(state: ShuttlePassListState, onAction: (ShuttlePassListAction) -> Unit) {
    val colors = ShuttleTheme.colors
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.primary)
        }
        state.sections.isEmpty() -> EmptyPasses(
            filter = state.filter,
            query = state.query,
            onClearSearch = { onAction(ShuttlePassListAction.QueryChange("")) }
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Dimens.PageMarginNarrow, vertical = Dimens.SpaceS)
        ) {
            // Header and card are one item, so a day's card is never split across items.
            state.sections.forEach { section ->
                item(key = section.key) {
                    PassDayCard(section, onOpenPass = { onAction(ShuttlePassListAction.OpenPass(it)) })
                }
            }
        }
    }
}

/** "Today · Tue, Oct 20" and one card of that day's passes. */
@Composable
private fun PassDayCard(section: PassDaySection, onOpenPass: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Column(modifier.fillMaxWidth().padding(top = Dimens.SpaceM)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpaceXs).padding(bottom = Dimens.SpaceS),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                section.title,
                style = ShuttleType.Label,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            Text(section.dateLabel, style = ShuttleType.CaptionNumeric, color = colors.textSecondary)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.RadiusCard),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            section.passes.forEachIndexed { index, pass ->
                if (index > 0) HorizontalDivider(Modifier.padding(horizontal = Dimens.SpaceM), color = colors.divider)
                PassRow(pass, onClick = { onOpenPass(pass.id) })
            }
        }
    }
}

@Composable
private fun EmptyPasses(filter: PassFilter, query: String, onClearSearch: () -> Unit) {
    val colors = ShuttleTheme.colors
    val message = when {
        query.isNotBlank() -> "No passes match “${query.trim()}”."
        filter == PassFilter.Late -> "No late passes."
        filter == PassFilter.NotSynced -> "Everything is synced. New passes appear here until they upload."
        else -> "No finished passes yet. Passes appear here once you complete a trip."
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.PageMargin, vertical = Dimens.SpaceXl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = ShuttleType.Body, color = colors.textSecondary, textAlign = TextAlign.Center)
        if (query.isNotBlank()) {
            TextButton(onClick = onClearSearch, modifier = Modifier.padding(top = Dimens.SpaceXs)) {
                Text("Clear search", style = ShuttleType.Label, color = colors.brandAccent)
            }
        }
    }
}

private fun previewPass(n: Int, summary: String, isLate: Boolean = false, isSynced: Boolean = false) = ShuttlePassItemUi(
    id = "$n", code = "SPT-2026-10-%04d".format(n), schedule = "", routeAndTrip = "",
    isLate = isLate, isSynced = isSynced, summary = summary
)

private val previewSections = listOf(
    PassDaySection("2026-10-20", "Today", "Tue, Oct 20", listOf(previewPass(12, "Villa Estrella · In · 07:45"))),
    PassDaySection(
        "2026-10-19", "Yesterday", "Mon, Oct 19",
        listOf(previewPass(11, "Mamatid · Out · 17:10", isLate = true), previewPass(10, "Villa Estrella · In · 07:40", isSynced = true))
    ),
    PassDaySection(
        "2026-10-16", "Friday", "Oct 16",
        listOf(
            previewPass(9, "Mamatid · Out · 17:05"),
            previewPass(8, "Villa Estrella · In · 07:42", isSynced = true),
            previewPass(7, "Mamatid · Out · 17:08", isSynced = true)
        )
    )
)

private val previewState = ShuttlePassListState(sections = previewSections, isLoading = false, pendingCount = 3, lateCount = 1)

@Preview(widthDp = 412, heightDp = 916)
@Preview(widthDp = 412, heightDp = 916, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShuttlePassListScreenPreview() = ShuttleAppTheme {
    ShuttlePassListScreen(previewState, onAction = {}, onNavigate = {})
}

@Preview(widthDp = 412, heightDp = 640)
@Composable
private fun ShuttlePassListScreenEmptyPreview() = ShuttleAppTheme(darkTheme = false) {
    ShuttlePassListScreen(ShuttlePassListState(isLoading = false), onAction = {}, onNavigate = {})
}

@Preview(widthDp = 412, heightDp = 640)
@Preview(widthDp = 412, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShuttlePassListScreenNoResultsPreview() = ShuttleAppTheme {
    ShuttlePassListScreen(previewState.copy(sections = emptyList(), query = "Calamba"), onAction = {}, onNavigate = {})
}

@Preview(widthDp = 412, heightDp = 640)
@Composable
private fun ShuttlePassListScreenLoadingPreview() = ShuttleAppTheme(darkTheme = false) {
    ShuttlePassListScreen(ShuttlePassListState(), onAction = {}, onNavigate = {})
}

@Preview(widthDp = 412, heightDp = 916)
@Composable
private fun ShuttlePassListScreenSyncFailedPreview() = ShuttleAppTheme(darkTheme = false) {
    ShuttlePassListScreen(
        previewState.copy(sync = SyncStatus.Failed("Cannot connect to network."), isOnline = false),
        onAction = {},
        onNavigate = {}
    )
}
