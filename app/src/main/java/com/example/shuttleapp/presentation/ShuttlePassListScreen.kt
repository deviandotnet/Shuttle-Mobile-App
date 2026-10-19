package com.example.shuttleapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.shuttleapp.presentation.components.ReauthDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.presentation.components.ActionDropdownMenu
import com.example.shuttleapp.presentation.components.InformationTextDialog
import com.example.shuttleapp.presentation.components.LoadingDialog
import com.example.shuttleapp.presentation.components.ObserveAsEvents
import com.example.shuttleapp.presentation.components.ShuttlePassCard
import com.example.shuttleapp.presentation.components.StyledSnackBarHost
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.events.ShuttlePassListAction
import com.example.shuttleapp.presentation.events.ShuttlePassListEvent
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.presentation.state.ShuttlePassListState
import com.example.shuttleapp.presentation.state.SyncStatus
import com.example.shuttleapp.presentation.viewmodel.ShuttlePassListViewModel
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import kotlinx.coroutines.launch

@Composable
fun ShuttlePassListRoot(
    viewModel: ShuttlePassListViewModel,
    onBack: () -> Unit,
    onOpenPass: (id: String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ShuttlePassListEvent.NavigateToPass -> onOpenPass(event.id)
            ShuttlePassListEvent.NavigateBack -> onBack()
        }
    }
    ShuttlePassListScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun ShuttlePassListScreen(state: ShuttlePassListState, onAction: (ShuttlePassListAction) -> Unit) {
    val colors = ShuttleTheme.colors
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { StyledSnackBarHost(hostState = snackBarHostState) },
        topBar = {
            TopBarComponent(
                title = "Completed Shuttle Pass List",
                onBack = { onAction(ShuttlePassListAction.Back) },
                action = {
                    ActionDropdownMenu(
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
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .background(colors.canvas),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConnectionBanners(state, onAction)
            FilterChoiceSegmentedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                selectedFilter = state.filter,
                onFilterSelected = { onAction(ShuttlePassListAction.SelectFilter(it)) }
            )
            HorizontalDivider(Modifier.fillMaxWidth())
            PassListContent(state, onAction)
        }
        SyncResultDialog(state.sync, onDismiss = { onAction(ShuttlePassListAction.DismissSyncResult) })
        state.reauth?.let { reauth ->
            ReauthDialog(
                state = reauth,
                onSubmit = { username, password -> onAction(ShuttlePassListAction.SubmitReauth(username, password)) },
                onDismiss = { onAction(ShuttlePassListAction.DismissReauth) }
            )
        }
    }
}

@Composable
private fun ConnectionBanners(state: ShuttlePassListState, onAction: (ShuttlePassListAction) -> Unit) {
    val colors = ShuttleTheme.colors
    if (!state.isOnline) {
        StatusBanner(
            text = "Offline — passes are saved on this device",
            background = colors.warningSoft,
            color = colors.warningText
        )
    } else if (state.needsSignIn) {
        StatusBanner(
            text = "Sign in again to upload. Tap here.",
            background = colors.errorSoft,
            color = colors.error,
            onClick = { onAction(ShuttlePassListAction.ShowReauth) }
        )
    }
}

@Composable
private fun StatusBanner(text: String, background: Color, color: Color, onClick: (() -> Unit)? = null) {
    Text(
        text = text,
        color = color,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun ColumnScope.PassListContent(state: ShuttlePassListState, onAction: (ShuttlePassListAction) -> Unit) {
    val colors = ShuttleTheme.colors
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.primary)
        }
        state.items.isEmpty() -> {
            Spacer(Modifier.weight(0.10f))
            Text(
                text = "No items to display",
                fontStyle = FontStyle.Italic,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.weight(0.90f))
        }
        else -> LazyColumn(
            modifier = Modifier
                .background(color = colors.canvas)
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            items(state.items, key = { it.id }) { item ->
                ShuttlePassCard(item = item, onClick = { onAction(ShuttlePassListAction.OpenPass(item.id)) })
            }
        }
    }
}

@Composable
private fun SyncResultDialog(sync: SyncStatus, onDismiss: () -> Unit) {
    val colors = ShuttleTheme.colors
    when (sync) {
        SyncStatus.Idle -> Unit
        SyncStatus.Running -> LoadingDialog()
        is SyncStatus.Success -> InformationTextDialog(
            onDismissRequest = onDismiss,
            dialogTitle = "Success!",
            dialogText = sync.message,
            icon = Icons.TwoTone.CheckCircle,
            iconTint = colors.success
        )
        is SyncStatus.Failed -> InformationTextDialog(
            onDismissRequest = onDismiss,
            dialogTitle = "Error!",
            dialogText = sync.message,
            icon = Icons.TwoTone.Cancel,
            iconTint = colors.error
        )
    }
}

@Composable
private fun FilterChoiceSegmentedButton(
    modifier: Modifier = Modifier,
    selectedFilter: PassFilter,
    onFilterSelected: (PassFilter) -> Unit
) {
    val options = listOf(
        "All" to PassFilter.All,
        "Late" to PassFilter.Late,
        "Not Synced" to PassFilter.NotSynced
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, (label, filter) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onFilterSelected(filter) },
                selected = filter == selectedFilter,
                label = { Text(label) }
            )
        }
    }
}

private val previewItems = listOf(
    ShuttlePassItemUi("1", "SPT-2025-01-0A1B2C3D4E5F", "2025-01-02  06:15 — 07:05", "Villa Estrella · Incoming", isLate = true, isSynced = false),
    ShuttlePassItemUi("2", "SPT-2025-01-9F8E7D6C5B4A", "2025-01-01  17:10 — 18:02", "Villa Estrella · Outgoing", isLate = false, isSynced = true)
)

@Preview
@Composable
private fun ShuttlePassListScreenPreview() {
    ShuttleAppTheme {
        ShuttlePassListScreen(ShuttlePassListState(items = previewItems, isLoading = false), onAction = {})
    }
}

@Preview
@Composable
private fun ShuttlePassListScreenEmptyPreview() {
    ShuttleAppTheme {
        ShuttlePassListScreen(ShuttlePassListState(isLoading = false), onAction = {})
    }
}

@Preview
@Composable
private fun ShuttlePassListScreenSyncFailedPreview() {
    ShuttleAppTheme {
        ShuttlePassListScreen(
            ShuttlePassListState(items = previewItems, isLoading = false, sync = SyncStatus.Failed("Cannot connect to network.")),
            onAction = {}
        )
    }
}
