package com.example.shuttleapp.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.components.CtaPlusIcon
import com.example.shuttleapp.presentation.components.LiveDot
import com.example.shuttleapp.presentation.components.PrimaryCtaButton
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme

/**
 * Pinned bar that keeps the dashboard's primary action reachable however long the list gets.
 * With [maxButtonWidth] set (wide layouts) the button keeps that width and sits at the end.
 */
@Composable
fun StickyActionBar(
    hasActiveTrip: Boolean,
    onClick: () -> Unit,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier,
    maxButtonWidth: Dp = Dp.Unspecified
) {
    val colors = ShuttleTheme.colors
    Column(modifier.fillMaxWidth().background(colors.canvas)) {
        HorizontalDivider(color = colors.divider)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceS + Dimens.SpaceXs),
            contentAlignment = Alignment.CenterEnd
        ) {
            PrimaryAction(hasActiveTrip, onClick, Modifier.widthIn(max = maxButtonWidth))
        }
    }
}

/** Resumes the in-progress pass when there is one, otherwise starts a new pass. */
@Composable
private fun PrimaryAction(hasActiveTrip: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (hasActiveTrip) {
        PrimaryCtaButton("View current trip", onClick, modifier, leading = { LiveDot() })
    } else {
        PrimaryCtaButton("New shuttle pass", onClick, modifier, leading = { CtaPlusIcon() })
    }
}

@Preview(widthDp = 360)
@Composable
private fun StickyActionBarNewPassPreview() {
    StickyActionBar(hasActiveTrip = false, onClick = {}, horizontalPadding = Dimens.PageMargin)
}

@Preview(widthDp = 360)
@Composable
private fun StickyActionBarLiveTripPreview() {
    StickyActionBar(hasActiveTrip = true, onClick = {}, horizontalPadding = Dimens.PageMargin)
}

@Preview(widthDp = 840)
@Composable
private fun StickyActionBarWidePreview() {
    StickyActionBar(hasActiveTrip = false, onClick = {}, horizontalPadding = Dimens.PageMargin, maxButtonWidth = 420.dp)
}
