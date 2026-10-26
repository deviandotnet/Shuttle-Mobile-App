package com.example.shuttleapp.presentation.components.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.components.PrimaryCtaButton
import com.example.shuttleapp.presentation.components.pass.PassengerManifestRow
import com.example.shuttleapp.presentation.events.QrScannerAction
import com.example.shuttleapp.presentation.state.QrScannerState
import com.example.shuttleapp.presentation.state.ScannerTripUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** Docked sheet under the camera: trip, live headcount, the newest rows, and Torch / Done. */
@Composable
fun ScannerSheet(state: QrScannerState, onAction: (QrScannerAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    val shape = RoundedCornerShape(topStart = Dimens.SpaceL, topEnd = Dimens.SpaceL)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(12.dp, shape)
            .clip(shape)
            .background(colors.canvas)
            .padding(start = Dimens.SpaceM, end = Dimens.SpaceM, top = 20.dp, bottom = Dimens.SpaceM)
    ) {
        TripLine(state.trip)
        Text(
            text = "${state.onBoardCount} on board",
            style = ShuttleType.Heading2.copy(fontFeatureSettings = "tnum"),
            color = colors.textPrimary,
            modifier = Modifier.padding(start = Dimens.SpaceS, end = Dimens.SpaceS, top = Dimens.SpaceXs)
        )
        Spacer(Modifier.height(10.dp))
        RecentRows(state)
        Spacer(Modifier.height(Dimens.SpaceM))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TorchButton(on = state.torchOn, enabled = state.torchEnabled, onClick = { onAction(QrScannerAction.ToggleTorch) })
            PrimaryCtaButton(
                label = "Done",
                onClick = { onAction(QrScannerAction.Done) },
                leading = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(22.dp)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TripLine(trip: ScannerTripUi) {
    val colors = ShuttleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        modifier = Modifier.padding(horizontal = Dimens.SpaceS)
    ) {
        Icon(Icons.Filled.Place, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
        Text(trip.routeLabel, style = ShuttleType.Caption, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Text(trip.timeLabel, style = ShuttleType.CaptionNumeric, color = colors.textSecondary)
    }
}

@Composable
private fun RecentRows(state: QrScannerState) {
    val colors = ShuttleTheme.colors
    val shape = RoundedCornerShape(Dimens.RadiusCard)
    Column(Modifier.fillMaxWidth().shadow(6.dp, shape).clip(shape).background(colors.surface)) {
        if (state.recentRows.isEmpty()) {
            Text(
                "Scanned passengers show up here.",
                style = ShuttleType.Body,
                color = colors.textSecondary,
                modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceM)
            )
        }
        state.recentRows.forEachIndexed { index, row ->
            PassengerManifestRow(
                row = row,
                isFirst = index == 0,
                isLast = index == state.recentRows.lastIndex,
                onEdit = null,
                onDelete = null,
                highlighted = row.id == state.highlightedRowId
            )
        }
    }
}

@Composable
private fun TorchButton(on: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = ShuttleTheme.colors
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(Dimens.RadiusButton),
        border = if (on && enabled) null else BorderStroke(1.5.dp, if (enabled) colors.textPrimary else colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (on) colors.primary else colors.surface,
            contentColor = if (on) colors.onPrimary else colors.textPrimary,
            disabledContainerColor = colors.surfaceSubtle,
            disabledContentColor = colors.textDisabled
        ),
        modifier = Modifier
            .width(132.dp)
            .height(Dimens.CtaHeight)
            .semantics { stateDescription = if (on) "On" else "Off" }
    ) {
        Icon(
            if (on) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
            contentDescription = null,
            tint = if (on && enabled) colors.success else LocalContentColor.current,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text("Torch", style = ShuttleType.Button)
    }
}
