package com.example.shuttleapp.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** One recent pass: doc icon, code and schedule, sync status, and a chevron. */
@Composable
fun RecentPassRow(pass: ShuttlePassItemUi, onClick: () -> Unit) {
    val colors = ShuttleTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClickLabel = "Open pass ${pass.code}", onClick = onClick)
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS + Dimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS + Dimens.SpaceXs)
    ) {
        Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f)) {
            Text(pass.code, style = ShuttleType.Label, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(pass.schedule, style = ShuttleType.Caption, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
            if (pass.isSynced) StatusPill("Synced", colors.successText, colors.successSoft, colors.successText)
            else StatusPill("Pending", colors.warning, colors.warningSoft, colors.warningText)
            if (pass.isLate) StatusPill("Late", colors.error, colors.errorSoft, colors.error)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.textSecondary)
    }
}

@Composable
private fun StatusPill(label: String, dot: Color, background: Color, text: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Text(label, style = ShuttleType.Caption, color = text, maxLines = 1)
    }
}
