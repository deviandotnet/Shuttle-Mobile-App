package com.example.shuttleapp.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** All / Late / Not synced, as a white segmented control; Late and Not synced show their counts when non-zero. */
@Composable
fun PassFilterTabs(
    selected: PassFilter,
    lateCount: Int,
    notSyncedCount: Int,
    onSelect: (PassFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    val shape = RoundedCornerShape(Dimens.RadiusButton)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.divider, shape)
            .padding(Dimens.SpaceXs)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)
    ) {
        FilterTab("All", 0, Color.Unspecified, Color.Unspecified, selected == PassFilter.All) { onSelect(PassFilter.All) }
        FilterTab("Late", lateCount, colors.errorSoft, colors.error, selected == PassFilter.Late) { onSelect(PassFilter.Late) }
        FilterTab("Not synced", notSyncedCount, colors.warningSoft, colors.warningText, selected == PassFilter.NotSynced) {
            onSelect(PassFilter.NotSynced)
        }
    }
}

@Composable
private fun RowScope.FilterTab(
    label: String,
    count: Int,
    badgeBackground: Color,
    badgeText: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = ShuttleTheme.colors
    Row(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) colors.surfaceQuiet else Color.Transparent)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            .semantics { if (count > 0) contentDescription = "$label, $count" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        Text(
            label,
            style = ShuttleType.Label,
            color = if (isSelected) colors.textPrimary else colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (count > 0) CountBadge(count, badgeBackground, badgeText)
    }
}

@Composable
private fun CountBadge(count: Int, background: Color, text: Color) {
    Box(
        modifier = Modifier
            .widthIn(min = 20.dp)
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(if (count > 99) "99+" else "$count", style = ShuttleType.CaptionNumeric, color = text, textAlign = TextAlign.Center)
    }
}

@Preview(widthDp = 380, showBackground = true)
@Preview(widthDp = 380, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PassFilterTabsPreview() = ShuttleAppTheme {
    Box(Modifier.background(ShuttleTheme.colors.canvas).padding(Dimens.SpaceM)) {
        PassFilterTabs(selected = PassFilter.All, lateCount = 1, notSyncedCount = 3, onSelect = {})
    }
}
