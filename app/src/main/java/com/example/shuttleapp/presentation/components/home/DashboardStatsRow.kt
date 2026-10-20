package com.example.shuttleapp.presentation.components.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

private data class Stat(
    val label: String,
    val value: Int,
    val dot: Color?,
    val onClickLabel: String,
    val onClick: (() -> Unit)?
)

/**
 * Passengers on the current trip, late passes and passes not yet synced. Each tile opens what it counts:
 * the current trip, or the pass list filtered to Late / Not synced. Passengers is inert without a trip.
 */
@Composable
fun DashboardStatsRow(
    passengerCount: Int,
    lateCount: Int,
    pendingSyncCount: Int,
    onOpenPassengers: (() -> Unit)?,
    onOpenLate: () -> Unit,
    onOpenNotSynced: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    val stats = listOf(
        Stat("Passengers", passengerCount, dot = null, "open current trip", onOpenPassengers),
        Stat("Late", lateCount, dot = colors.error, "show late passes", onOpenLate),
        Stat("Not synced", pendingSyncCount, dot = colors.warning, "show passes not synced", onOpenNotSynced)
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusCard),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = Dimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stats.forEachIndexed { index, stat ->
                if (index > 0) {
                    Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical = Dimens.SpaceS).background(colors.divider))
                }
                StatTile(stat, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun StatTile(stat: Stat, modifier: Modifier) {
    val colors = ShuttleTheme.colors
    val open = stat.onClick
    Column(
        modifier
            .padding(horizontal = Dimens.SpaceXs)
            .clip(RoundedCornerShape(Dimens.RadiusButton))
            .then(if (open != null) Modifier.clickable(role = Role.Button, onClick = open) else Modifier)
            .clearAndSetSemantics {
                contentDescription = "${stat.label}: ${stat.value}"
                if (open != null) {
                    role = Role.Button
                    onClick(label = stat.onClickLabel) { open(); true }
                }
            }
            .heightIn(min = Dimens.MinTouch)
            .padding(horizontal = Dimens.SpaceS + Dimens.SpaceXs, vertical = Dimens.SpaceS)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            stat.dot?.let { Box(Modifier.padding(end = 6.dp).size(8.dp).background(it, CircleShape)) }
            Text(stat.label, style = ShuttleType.Caption, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpaceXs),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${stat.value}", style = ShuttleType.Metric, color = colors.textPrimary, maxLines = 1)
            if (open != null) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(widthDp = 360, showBackground = true)
@Preview(widthDp = 360, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardStatsRowPreview() = ShuttleAppTheme {
    DashboardStatsRow(24, 1, 3, onOpenPassengers = {}, onOpenLate = {}, onOpenNotSynced = {}, modifier = Modifier.padding(16.dp))
}

@Preview(widthDp = 320, showBackground = true, fontScale = 1.5f)
@Composable
private fun DashboardStatsRowNoTripLargeFontPreview() = ShuttleAppTheme(darkTheme = false) {
    DashboardStatsRow(0, 12, 140, onOpenPassengers = null, onOpenLate = {}, onOpenNotSynced = {}, modifier = Modifier.padding(16.dp))
}
