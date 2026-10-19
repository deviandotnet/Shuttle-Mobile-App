package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.ShuttleTheme

@Composable
fun ActionDropdownMenu(
    onUploadAll: () -> Unit,
    onDeleteAll: () -> Unit
) {
    val colors = ShuttleTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .wrapContentSize(Alignment.TopEnd)
        .background(colors.brandBand)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More actions",
                tint = colors.onBrandBand
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colors.brandBand,
            offset = DpOffset(4.dp,4.dp),
            shadowElevation = 8.dp,
        ) {
            DropdownMenuItem(
                text = { Text(text = "Upload All Unsynced", style = TextStyle(color = colors.onBrandBand)) },
                onClick = {
                    expanded = false
                    onUploadAll()
                }
            )
            DropdownMenuItem(
                text = { Text(text = "Delete All Synced", style = TextStyle(color = colors.onBrandBand)) },
                onClick = {
                    expanded = false
                    onDeleteAll()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionDropdownMenuPreview() {
    ActionDropdownMenu(
        onUploadAll = { } ,
        onDeleteAll = { }
    )
}