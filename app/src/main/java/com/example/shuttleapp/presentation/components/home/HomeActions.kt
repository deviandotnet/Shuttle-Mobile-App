package com.example.shuttleapp.presentation.components.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.components.LiveDot
import com.example.shuttleapp.presentation.mapper.DashboardFormat
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** Home's one primary action: "New pass", or "Current trip" while a draft is open. */
@Composable
fun HomeFab(hasActiveTrip: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(Dimens.RadiusCard),
        containerColor = colors.primary,
        contentColor = colors.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
        icon = {
            if (hasActiveTrip) LiveDot() else Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(24.dp))
        },
        text = { Text(if (hasActiveTrip) "Current trip" else "New pass", style = ShuttleType.Button) }
    )
}

/** Amber notice on Home while finalized passes wait to upload. Hidden by the caller when there are none. */
@Composable
fun PendingSyncCallout(count: Int, isSyncing: Boolean, onSyncNow: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusCard),
        color = colors.warningSoft
    ) {
        Row(
            modifier = Modifier.padding(start = Dimens.SpaceM, end = Dimens.SpaceS, top = Dimens.SpaceS, bottom = Dimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
        ) {
            Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = colors.warning, modifier = Modifier.size(28.dp))
            Text(
                DashboardFormat.pendingCallout(count),
                style = ShuttleType.Label.copy(fontSize = ShuttleType.BodyLarge.fontSize),
                color = colors.warningText,
                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }
            )
            TextButton(onClick = onSyncNow, enabled = !isSyncing, modifier = Modifier.heightIn(min = Dimens.MinTouch)) {
                if (isSyncing) {
                    CircularProgressIndicator(color = colors.textPrimary, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Text("Sync now", style = ShuttleType.Label.copy(fontSize = ShuttleType.BodyLarge.fontSize), color = colors.textPrimary)
                }
            }
        }
    }
}

@Preview(widthDp = 360, showBackground = true)
@Preview(widthDp = 360, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeActionsPreview() = ShuttleAppTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End) {
        PendingSyncCallout(count = 3, isSyncing = false, onSyncNow = {})
        PendingSyncCallout(count = 1, isSyncing = true, onSyncNow = {})
        HomeFab(hasActiveTrip = false, onClick = {})
        HomeFab(hasActiveTrip = true, onClick = {})
    }
}
