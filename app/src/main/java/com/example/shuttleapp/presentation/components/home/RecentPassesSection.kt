package com.example.shuttleapp.presentation.components.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** "Recent passes" header plus one card of rows, with loading, error (with Retry) and empty states. */
@Composable
fun RecentPassesSection(
    passes: List<ShuttlePassItemUi>,
    isLoading: Boolean,
    error: String?,
    onOpenPass: (String) -> Unit,
    onViewAll: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Recent passes",
                style = ShuttleType.Section,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            TextButton(onClick = onViewAll) {
                Text("View all", style = ShuttleType.Label.copy(fontSize = ShuttleType.BodyLarge.fontSize), color = colors.brandAccent)
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpaceS),
            shape = RoundedCornerShape(Dimens.RadiusCard),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            when {
                isLoading -> CenteredBox { CircularProgressIndicator(color = colors.primary) }
                error != null -> CenteredBox {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Message("Couldn't load your passes.", colors.error)
                        TextButton(onClick = onRetry, modifier = Modifier.padding(top = Dimens.SpaceXs)) {
                            Text("Retry", style = ShuttleType.Label, color = colors.primary)
                        }
                    }
                }
                passes.isEmpty() -> CenteredBox { Message("No completed passes yet. Tap New pass to start one.", colors.textSecondary) }
                else -> passes.forEachIndexed { index, pass ->
                    if (index > 0) HorizontalDivider(Modifier.padding(horizontal = Dimens.SpaceM), color = colors.divider)
                    RecentPassRow(pass, onClick = { onOpenPass(pass.id) })
                }
            }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(Dimens.SpaceL), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun Message(text: String, color: Color) {
    Text(text, style = ShuttleType.Body, color = color, textAlign = TextAlign.Center)
}

private val previewPasses = listOf(
    ShuttlePassItemUi("1", "SPT-0125-00001", "Jan 02, 2025 10:45", "Villa Estrella · Incoming", isLate = false, isSynced = true),
    ShuttlePassItemUi("2", "SPT-0125-00002", "Jan 02, 2025 08:20", "Villa Estrella · Outgoing", isLate = true, isSynced = false),
    ShuttlePassItemUi("3", "SPT-0125-00003", "Jan 01, 2025 17:10", "Mamatid · Incoming", isLate = false, isSynced = false)
)

@Preview(widthDp = 360, showBackground = true)
@Preview(widthDp = 360, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecentPassesPreview() = ShuttleAppTheme {
    RecentPassesSection(previewPasses, isLoading = false, error = null, onOpenPass = {}, onViewAll = {}, onRetry = {}, modifier = Modifier.padding(16.dp))
}

@Preview(widthDp = 360, showBackground = true)
@Composable
private fun RecentPassesEmptyPreview() {
    RecentPassesSection(emptyList(), isLoading = false, error = null, onOpenPass = {}, onViewAll = {}, onRetry = {}, modifier = Modifier.padding(16.dp))
}

@Preview(widthDp = 360, showBackground = true)
@Preview(widthDp = 360, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecentPassesErrorPreview() = ShuttleAppTheme {
    RecentPassesSection(emptyList(), isLoading = false, error = "boom", onOpenPass = {}, onViewAll = {}, onRetry = {}, modifier = Modifier.padding(16.dp))
}
