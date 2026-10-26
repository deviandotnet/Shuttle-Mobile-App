package com.example.shuttleapp.presentation.components.pass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

internal object PassIcons {
    val Shuttle: ImageVector = Icons.Filled.DirectionsBus
    val Driver: ImageVector = Icons.Filled.Person
}

/** Icon + primary text, optionally followed by " · secondary" in the muted colour. */
@Composable
internal fun FooterLine(icon: ImageVector, primary: String, secondary: String?, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        Icon(icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
        Text(
            text = buildAnnotatedString {
                append(primary.ifBlank { "—" })
                if (!secondary.isNullOrBlank()) {
                    withStyle(SpanStyle(color = colors.textSecondary)) { append(" · $secondary") }
                }
            },
            style = ShuttleType.Label.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
