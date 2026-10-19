package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.domain.model.ThemeMode
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** System / Light / Dark picker. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    val options = ThemeMode.entries
    Column(modifier) {
        Text("Appearance", style = ShuttleType.Label, color = colors.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selected,
                    onClick = { onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = colors.surfaceQuiet,
                        activeContentColor = colors.textPrimary,
                        activeBorderColor = colors.border,
                        inactiveContainerColor = colors.surface,
                        inactiveContentColor = colors.textSecondary,
                        inactiveBorderColor = colors.border
                    )
                ) {
                    Text(mode.name, style = ShuttleType.Caption)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeModeSelectorPreview() {
    ShuttleAppTheme(darkTheme = false) { ThemeModeSelector(ThemeMode.System, {}) }
}
