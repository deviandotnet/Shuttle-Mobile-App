package com.example.shuttleapp.presentation.components.home

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** "Recent shuttle passes" header plus one card of rows, with loading, error and empty states. */
@Composable
fun RecentPassesSection(
    passes: List<ShuttlePassItemUi>,
    totalCount: Int,
    isLoading: Boolean,
    error: String?,
    onOpenPass: (String) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Recent shuttle passes", style = ShuttleType.Section, color = colors.textPrimary, modifier = Modifier.weight(1f))
            TextButton(onClick = onViewAll) {
                Text(if (totalCount > 0) "View all ($totalCount)" else "View all", style = ShuttleType.Label, color = colors.brandAccent, textDecoration = TextDecoration.Underline)
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
                error != null -> CenteredBox { Message("Couldn't load your passes. Please try again.", colors.error) }
                passes.isEmpty() -> CenteredBox { Message("No completed passes yet. Start one with the button below.", colors.textSecondary) }
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
@Composable
private fun RecentPassesPreview() {
    RecentPassesSection(previewPasses, totalCount = 12, isLoading = false, error = null, onOpenPass = {}, onViewAll = {}, modifier = Modifier.padding(16.dp))
}

@Preview(widthDp = 360, showBackground = true)
@Composable
private fun RecentPassesEmptyPreview() {
    RecentPassesSection(emptyList(), totalCount = 0, isLoading = false, error = null, onOpenPass = {}, onViewAll = {}, modifier = Modifier.padding(16.dp))
}

@Preview(widthDp = 360, showBackground = true)
@Composable
private fun RecentPassesErrorPreview() {
    RecentPassesSection(emptyList(), totalCount = 0, isLoading = false, error = "boom", onOpenPass = {}, onViewAll = {}, modifier = Modifier.padding(16.dp))
}
