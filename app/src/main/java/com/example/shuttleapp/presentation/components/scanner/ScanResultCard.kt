package com.example.shuttleapp.presentation.components.scanner

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuttleapp.presentation.state.ScanResultUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleColors
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

private data class ResultLook(
    val container: Color,
    val iconBackground: Color,
    val iconTint: Color,
    val icon: ImageVector,
    val title: String,
    val body: String,
    val badge: String?
)

/** Outcome of the last scan (mockups 14a–14c). Announced to screen readers when it appears. */
@Composable
fun ScanResultCard(result: ScanResultUi, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    val look = result.look(colors)
    val shape = RoundedCornerShape(Dimens.RadiusCard)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .shadow(10.dp, shape)
            .background(look.container, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(look.iconBackground), contentAlignment = Alignment.Center) {
            Icon(look.icon, contentDescription = null, tint = look.iconTint, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(look.title, style = ShuttleType.BodyLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp), color = colors.textPrimary)
            Text(
                look.body,
                style = ShuttleType.Label.copy(fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum"),
                color = colors.textPrimary.copy(alpha = 0.74f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        look.badge?.let {
            Text(
                it,
                style = ShuttleType.CaptionNumeric.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary.copy(alpha = 0.74f),
                modifier = Modifier.align(Alignment.Top).padding(top = 3.dp)
            )
        }
    }
}

private fun ScanResultUi.look(colors: ShuttleColors): ResultLook = when (this) {
    is ScanResultUi.Boarded -> ResultLook(
        colors.successSoft, colors.success, colors.onSuccess, Icons.Filled.Check,
        "Boarded", "$code · $time", "#$number"
    )
    is ScanResultUi.AlreadyOnBoard -> ResultLook(
        colors.warningSoft, colors.warning, colors.onSuccess, Icons.Filled.Replay,
        "Already on board", "$code boarded at $time as #$number", null
    )
    ScanResultUi.NotPassenger -> ResultLook(
        colors.errorSoft, colors.error, colors.onError, Icons.Filled.Block,
        "Not a passenger QR", "Ask for the QR on their company ID.", null
    )
}
