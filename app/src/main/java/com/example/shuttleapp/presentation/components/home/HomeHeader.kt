package com.example.shuttleapp.presentation.components.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/**
 * Midnight band at the top of Home: avatar, greeting and settings. It draws behind the status bar,
 * and [bottomExtra] leaves room for the route card that overlaps it.
 */
@Composable
fun HomeHeader(
    greeting: String,
    initials: String,
    onOpenProfile: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    bottomExtra: Dp = 48.dp
) {
    val colors = ShuttleTheme.colors
    Box(modifier = modifier.fillMaxWidth().background(colors.brandBand)) {
        MapLines(Modifier.matchParentSize())
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(contentPadding)
                .padding(top = Dimens.SpaceM, bottom = Dimens.SpaceM + bottomExtra),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
        ) {
            InitialsAvatar(initials = initials, onClick = onOpenProfile)
            Column(Modifier.weight(1f)) {
                Text(greeting, style = ShuttleType.AppBar, color = colors.onBrandBand, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    "Here's today's shuttle activity",
                    style = ShuttleType.Body,
                    color = colors.onBrandBandMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onOpenProfile, modifier = Modifier.size(Dimens.MinTouch)) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = colors.onBrandBand)
            }
        }
    }
}

@Composable
private fun InitialsAvatar(initials: String, onClick: () -> Unit) {
    val colors = ShuttleTheme.colors
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(colors.surfaceQuiet)
            .clickable(onClickLabel = "Open profile menu", role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(initials.ifBlank { "?" }, style = ShuttleType.Label, color = colors.textPrimary)
    }
}

/** Faint route lines across the band, scaled to its size. */
@Composable
private fun MapLines(modifier: Modifier) {
    val colors = ShuttleTheme.colors
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx())
        val line = colors.brandBandLine
        val w = size.width
        val h = size.height
        drawPath(Path().apply {
            moveTo(0f, h * 0.35f)
            cubicTo(w * 0.3f, h * 0.15f, w * 0.55f, h * 0.7f, w, h * 0.45f)
        }, line, style = stroke)
        drawPath(Path().apply {
            moveTo(w * 0.15f, h)
            cubicTo(w * 0.35f, h * 0.55f, w * 0.7f, h * 0.95f, w * 0.95f, 0f)
        }, line, style = stroke)
        drawPath(Path().apply {
            moveTo(w * 0.55f, h)
            quadraticTo(w * 0.8f, h * 0.6f, w, h * 0.75f)
        }, line, style = stroke)
        drawCircle(line, radius = h * 0.35f, center = Offset(w * 0.9f, h * 0.2f), style = stroke)
    }
}

@Preview(widthDp = 360)
@Composable
private fun HomeHeaderPreview() {
    HomeHeader(
        greeting = "Good morning, Elmo",
        initials = "ER",
        onOpenProfile = {},
        contentPadding = PaddingValues(horizontal = Dimens.PageMargin)
    )
}
