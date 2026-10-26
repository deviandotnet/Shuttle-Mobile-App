package com.example.shuttleapp.presentation.components.pass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.components.home.BusIllustration
import com.example.shuttleapp.presentation.state.PassCardUi
import com.example.shuttleapp.presentation.state.PassStatus
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

private val StopHeight = 56.dp
private val RailWidth = 20.dp
/** Distance from the top of a stop row to the centre of its marker. */
private val MarkerCenter = 33.dp

/** The pass at a glance: status and date, the From → To timeline with its times, then the shuttle and driver. */
@Composable
fun PassSummaryCard(card: PassCardUi, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusCard),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = Dimens.SpaceM)
        ) {
            PassStatusPill(card.status)
            Spacer(Modifier.weight(1f))
            Text(card.dateLabel, style = ShuttleType.CaptionNumeric, color = colors.textSecondary)
        }
        StopTimeline(card, Modifier.padding(start = 20.dp, end = 20.dp, top = Dimens.SpaceS, bottom = Dimens.SpaceS))
        HorizontalDivider(color = colors.divider)
        PassFooter(card)
    }
}

@Composable
fun PassStatusPill(status: PassStatus, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    val (background, content, dot) = when (status) {
        PassStatus.NotDeparted -> Triple(colors.warningSoft, colors.warningText, colors.warning)
        PassStatus.InTransit -> Triple(colors.successSoft, colors.successText, colors.successText)
        PassStatus.Completed -> Triple(colors.surfaceQuiet, colors.textPrimary, colors.brandAccent)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
        Text(status.label, style = ShuttleType.Caption.copy(fontWeight = FontWeight.SemiBold), color = content)
    }
}

@Composable
private fun StopTimeline(card: PassCardUi, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Box(modifier.fillMaxWidth()) {
        // Dotted rail between the two markers, drawn behind them.
        Canvas(Modifier.matchParentSize()) {
            val x = (RailWidth / 2).toPx()
            drawLine(
                color = colors.border,
                start = Offset(x, (MarkerCenter + 10.dp).toPx()),
                end = Offset(x, (StopHeight + MarkerCenter - 10.dp).toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(1f, 5.dp.toPx()))
            )
        }
        Column {
            StopRow("From", card.fromLabel, card.fromIsGeneric, "Departure", card.departure, card.hasDeparture, isOrigin = true)
            StopRow("To", card.toLabel, card.toIsGeneric, "Arrival", card.arrival, card.hasArrival, isOrigin = false)
        }
    }
}

@Composable
private fun StopRow(
    label: String,
    place: String,
    placeIsGeneric: Boolean,
    timeLabel: String,
    time: String,
    hasTime: Boolean,
    isOrigin: Boolean
) {
    val colors = ShuttleTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(StopHeight)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label $place, $timeLabel ${if (hasTime) time else "not recorded"}"
            }
    ) {
        Box(Modifier.width(RailWidth).padding(top = MarkerCenter - 6.dp), contentAlignment = Alignment.TopCenter) {
            StopMarker(isOrigin)
        }
        Column(Modifier.weight(1f).padding(start = 14.dp, top = 6.dp)) {
            Text(label.uppercase(), style = ShuttleType.Overline, color = colors.textSecondary)
            Text(
                text = place.ifBlank { "—" },
                style = ShuttleType.BodyLarge.copy(fontWeight = if (placeIsGeneric) FontWeight.Medium else FontWeight.SemiBold),
                color = if (placeIsGeneric) colors.textSecondary else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(Modifier.padding(start = Dimens.SpaceS, top = 6.dp), horizontalAlignment = Alignment.End) {
            Text(timeLabel.uppercase(), style = ShuttleType.Overline, color = colors.textSecondary)
            Text(
                text = time,
                style = ShuttleType.Section.copy(
                    fontFeatureSettings = if (hasTime) "tnum" else null,
                    fontWeight = if (hasTime) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (hasTime) colors.textPrimary else colors.textDisabled,
                textAlign = TextAlign.End
            )
        }
    }
}

/** Origin: mint ring. Destination: solid dot with a soft halo. */
@Composable
private fun StopMarker(isOrigin: Boolean) {
    val colors = ShuttleTheme.colors
    if (isOrigin) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(colors.surface).border(3.dp, colors.successText, CircleShape))
    } else {
        Box(Modifier.size(18.dp).clip(CircleShape).background(colors.surfaceQuiet), contentAlignment = Alignment.Center) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(colors.primary))
        }
    }
}

@Composable
private fun PassFooter(card: PassCardUi) {
    Box(Modifier.fillMaxWidth().height(84.dp)) {
        BusIllustration(Modifier.align(Alignment.BottomEnd).width(148.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
            // Keeps the text clear of the bus.
            modifier = Modifier.padding(start = 20.dp, end = 132.dp, top = 14.dp)
        ) {
            FooterLine(PassIcons.Shuttle, card.plateNumber, card.provider)
            FooterLine(PassIcons.Driver, card.driver, secondary = null)
        }
    }
}
