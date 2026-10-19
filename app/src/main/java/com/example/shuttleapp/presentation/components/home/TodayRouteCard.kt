package com.example.shuttleapp.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** "Today's route" hero card. The bus scales with the card width and sits behind the text. */
@Composable
fun TodayRouteCard(
    hasActiveTrip: Boolean,
    routeTitle: String,
    departure: String,
    lateCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusCard),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // The bus takes ~62% of the width (capped on tablets). Only its top ~15% (clouds) may sit
            // behind the text, so the bus itself never collides with the title.
            val busWidth = (maxWidth * 0.62f).coerceAtMost(320.dp)
            // Drawn first so the text always paints above the clouds.
            BusIllustration(
                Modifier
                    .align(Alignment.BottomEnd)
                    .width(busWidth)
            )
            Column(Modifier.padding(Dimens.SpaceL)) {
                Text("TODAY'S ROUTE", style = ShuttleType.Overline, color = colors.textSecondary)
                Text(
                    text = if (hasActiveTrip) routeTitle.ifBlank { "Route not set" } else "No active trip",
                    style = ShuttleType.Heading2,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Dimens.SpaceS)
                )
                RouteSubtitle(hasActiveTrip, departure)
                if (lateCount > 0) LateChip(lateCount, Modifier.padding(top = Dimens.SpaceS))
                // Reserve the lower 85% of the illustration's height (its aspect is 300:170) below the text.
                Box(Modifier.size(width = 1.dp, height = busWidth * (170f / 300f) * 0.85f - Dimens.SpaceL))
            }
        }
    }
}

@Composable
private fun RouteSubtitle(hasActiveTrip: Boolean, departure: String) {
    val colors = ShuttleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        modifier = Modifier.padding(top = Dimens.SpaceS)
    ) {
        if (hasActiveTrip) {
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
            Text(
                text = if (departure.isBlank()) "Departure not set" else "$departure departure",
                style = ShuttleType.BodyLarge.copy(fontFeatureSettings = "tnum"),
                color = colors.textSecondary
            )
        } else {
            Text("Start a shuttle pass to begin today's trip", style = ShuttleType.Body, color = colors.textSecondary)
        }
    }
}

@Composable
private fun LateChip(count: Int, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Text(
        text = "$count late ${if (count == 1) "pass" else "passes"}",
        style = ShuttleType.Caption,
        color = colors.warningText,
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(colors.warningSoft)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Preview(widthDp = 360)
@Composable
private fun TodayRouteCardPreview() {
    TodayRouteCard(true, "Villa Estrella · Incoming", "16:30", lateCount = 2, modifier = Modifier.padding(16.dp))
}

@Preview(widthDp = 360)
@Composable
private fun TodayRouteCardNoTripPreview() {
    TodayRouteCard(false, "", "", lateCount = 0, modifier = Modifier.padding(16.dp))
}
