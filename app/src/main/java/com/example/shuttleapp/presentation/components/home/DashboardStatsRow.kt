package com.example.shuttleapp.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

private data class Stat(val value: Int, val label: String, val icon: ImageVector, val tint: Color)

/** Passengers / active pass / pending sync. On narrower cards the icon stacks above the number. */
@Composable
fun DashboardStatsRow(
    passengerCount: Int,
    activePassCount: Int,
    pendingSyncCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    val stats = listOf(
        Stat(passengerCount, if (passengerCount == 1) "passenger" else "passengers", Icons.Rounded.Groups, colors.successText),
        Stat(activePassCount, "active pass", Icons.Rounded.ConfirmationNumber, colors.brandAccent),
        Stat(pendingSyncCount, "pending sync", Icons.Rounded.CloudUpload, colors.warning)
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusCard),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BoxWithConstraints {
            // Side-by-side needs ~130dp per stat for "passengers" at 13sp; narrower cards stack.
            val stacked = maxWidth < 400.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = Dimens.SpaceM, horizontal = Dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                stats.forEachIndexed { index, stat ->
                    if (index > 0) {
                        Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical = Dimens.SpaceXs).background(colors.divider))
                    }
                    StatItem(stat, stacked, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatItem(stat: Stat, stacked: Boolean, modifier: Modifier) {
    val a11y = Modifier.clearAndSetSemantics { contentDescription = "${stat.value} ${stat.label}" }
    if (stacked) {
        Column(modifier.then(a11y), horizontalAlignment = Alignment.CenterHorizontally) {
            StatIcon(stat)
            StatText(stat, TextAlign.Center, Modifier.padding(top = Dimens.SpaceS))
        }
    } else {
        Row(
            modifier.then(a11y).padding(horizontal = Dimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS + Dimens.SpaceXs)
        ) {
            StatIcon(stat)
            StatText(stat, TextAlign.Start)
        }
    }
}

@Composable
private fun StatIcon(stat: Stat) {
    Icon(stat.icon, contentDescription = null, tint = stat.tint, modifier = Modifier.size(28.dp))
}

@Composable
private fun StatText(stat: Stat, align: TextAlign, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Column(modifier) {
        Text("${stat.value}", style = ShuttleType.Metric, color = colors.textPrimary, maxLines = 1, textAlign = align, modifier = Modifier.fillMaxWidth())
        Text(stat.label, style = ShuttleType.Caption, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = align, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(widthDp = 440)
@Composable
private fun DashboardStatsRowPreview() {
    DashboardStatsRow(24, 1, 3, Modifier.padding(16.dp))
}

@Preview(widthDp = 360)
@Composable
private fun DashboardStatsRowNarrowPreview() {
    DashboardStatsRow(24, 1, 3, Modifier.padding(16.dp))
}
