package com.example.shuttleapp.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shuttleapp.presentation.components.ConnectionBanner
import com.example.shuttleapp.presentation.components.ObserveAsEvents
import com.example.shuttleapp.presentation.components.ShuttlePassCard
import com.example.shuttleapp.presentation.components.UploadDialogs
import com.example.shuttleapp.presentation.components.home.HomeBottomBar
import com.example.shuttleapp.presentation.components.home.HomeNavItem
import com.example.shuttleapp.presentation.components.home.TabTopBar
import com.example.shuttleapp.presentation.events.SyncAction
import com.example.shuttleapp.presentation.events.SyncEvent
import com.example.shuttleapp.presentation.mapper.DashboardFormat
import com.example.shuttleapp.presentation.state.PassUploadState
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.presentation.state.SyncState
import com.example.shuttleapp.presentation.viewmodel.SyncViewModel
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

@Composable
fun SyncRoot(
    viewModel: SyncViewModel,
    onOpenPass: (id: String) -> Unit,
    onNavigate: (HomeNavItem) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SyncEvent.NavigateToPass -> onOpenPass(event.id)
        }
    }
    SyncScreen(state = state, onAction = viewModel::onAction, onNavigate = onNavigate)
}

/** Sync tab: what is waiting to upload, when this device last synced, and "Sync now". */
@Composable
fun SyncScreen(state: SyncState, onAction: (SyncAction) -> Unit, onNavigate: (HomeNavItem) -> Unit) {
    val colors = ShuttleTheme.colors
    Scaffold(
        containerColor = colors.canvas,
        topBar = { TabTopBar("Sync") },
        bottomBar = { HomeBottomBar(HomeNavItem.Sync, state.pendingCount, onNavigate) }
    ) { padding ->
        Column(Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize()) {
            ConnectionBanner(
                isOnline = state.upload.isOnline,
                needsSignIn = state.upload.needsSignIn,
                onSignIn = { onAction(SyncAction.ShowReauth) }
            )
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else {
                SyncList(state, onAction)
            }
        }
        UploadDialogs(
            upload = state.upload,
            onDismissResult = { onAction(SyncAction.DismissSyncResult) },
            onSubmitReauth = { username, password -> onAction(SyncAction.SubmitReauth(username, password)) },
            onDismissReauth = { onAction(SyncAction.DismissReauth) }
        )
    }
}

@Composable
private fun SyncList(state: SyncState, onAction: (SyncAction) -> Unit) {
    val colors = ShuttleTheme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Dimens.PageMarginNarrow, vertical = Dimens.SpaceM),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
    ) {
        item(key = "summary") { SyncSummaryCard(state, onSyncNow = { onAction(SyncAction.SyncNow) }) }
        if (state.pending.isEmpty()) {
            item(key = "empty") {
                Text(
                    "Every finished pass is uploaded. New passes appear here until they sync.",
                    style = ShuttleType.Body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(Dimens.SpaceL)
                )
            }
        } else {
            item(key = "title") {
                Text(
                    "Waiting to upload",
                    style = ShuttleType.Section,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = Dimens.SpaceM).semantics { heading() }
                )
            }
            items(state.pending, key = { it.id }) { item ->
                ShuttlePassCard(item = item, onClick = { onAction(SyncAction.OpenPass(item.id)) })
            }
        }
    }
}

@Composable
private fun SyncSummaryCard(state: SyncState, onSyncNow: () -> Unit) {
    val colors = ShuttleTheme.colors
    val pending = state.pendingCount > 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusCard),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(Dimens.SpaceM)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
                Icon(
                    if (pending) Icons.Rounded.CloudUpload else Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = if (pending) colors.warning else colors.successText,
                    modifier = Modifier.size(32.dp)
                )
                Column(Modifier.weight(1f)) {
                    Text(DashboardFormat.pendingShort(state.pendingCount), style = ShuttleType.Section, color = colors.textPrimary)
                    Text(state.lastSynced, style = ShuttleType.Body, color = colors.textSecondary)
                }
            }
            if (pending) {
                Button(
                    onClick = onSyncNow,
                    enabled = !state.isSyncing,
                    modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpaceM).heightIn(min = Dimens.CtaHeight),
                    shape = RoundedCornerShape(Dimens.RadiusButton),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.success, contentColor = colors.onSuccess)
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(22.dp))
                    Text("Sync now", style = ShuttleType.Button, modifier = Modifier.padding(start = Dimens.SpaceS))
                }
            }
        }
    }
}

private val previewPending = listOf(
    ShuttlePassItemUi("1", "SPT-1026-0A1B2C3D4E5F", "2026-10-20  07:45 — 08:30", "Villa Estrella · Incoming", isLate = false, isSynced = false),
    ShuttlePassItemUi("2", "SPT-1026-9F8E7D6C5B4A", "2026-10-19  17:10 — 18:02", "Mamatid · Outgoing", isLate = true, isSynced = false)
)

@Preview(widthDp = 360, heightDp = 720)
@Preview(widthDp = 360, heightDp = 720, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SyncScreenPreview() = ShuttleAppTheme {
    SyncScreen(
        SyncState(pending = previewPending, isLoading = false, lastSynced = "Last synced today, 10:42"),
        onAction = {},
        onNavigate = {}
    )
}

@Preview(widthDp = 360, heightDp = 720)
@Preview(widthDp = 360, heightDp = 720, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SyncScreenAllSyncedOfflinePreview() = ShuttleAppTheme {
    SyncScreen(
        SyncState(isLoading = false, lastSynced = "Last synced yesterday, 18:05", upload = PassUploadState(isOnline = false)),
        onAction = {},
        onNavigate = {}
    )
}
