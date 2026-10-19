@file:JvmName("DialogsKt")

package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import com.example.shuttleapp.ui.theme.ShuttleTheme


@Composable
fun EditDeleteChoiceDialog(onEdit:() -> Unit, onDelete:()-> Unit, onDismiss: () -> Unit) {
    val colors = ShuttleTheme.colors

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )
            {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            onEdit()
                            onDismiss()

                        }
                ) {
                    Text("Edit")
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            showDeleteConfirmation = true
                        }
                ) {
                    Text("Delete")
                }
            }

        }
    }

    if(showDeleteConfirmation) {

        ConfirmBasicDialog(
            content = "Are you sure you want to delete? This cannot be undone",
            onDismiss = {
                showDeleteConfirmation = false
                onDismiss() },
            onConfirm = {
                onDelete()
            })
    }

}

@Composable
fun LoadingDialog() {
    val colors = ShuttleTheme.colors
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .size(120.dp)
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.primary,
                    strokeWidth = 4.dp, // Example stroke width
                    modifier = Modifier.size(24.dp) // Set size for the indicator
                )
            }
        }
    }
}

@Composable
fun InformationTextDialog(
    onDismissRequest: () -> Unit,
    dialogTitle: String,
    dialogText:  String,
    icon: ImageVector,
    iconTint: Color
) {
    val colors = ShuttleTheme.colors
    LaunchedEffect(true) {
        delay(3000)
        onDismissRequest()
    }

    AlertDialog(
        icon = {
            Icon(modifier = Modifier.size(64.dp), imageVector = icon, contentDescription = "Example Icon", tint = iconTint)
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(dialogText, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
        },
        dismissButton = {
            TextButton(onClick = {onDismissRequest()}) {
                Text("Dismiss", color = colors.primary)
            }
        }
    )
}

@Composable
fun ConfirmBasicDialog(
    content: String ,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = ShuttleTheme.colors

    AlertDialog(
        containerColor = colors.surface,
        onDismissRequest = onDismiss,
        text = {
            Text(text = content,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth())
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .clickable { onDismiss() }
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable {
                        onConfirm()
                        onDismiss()
                    }
            ) {
                Text("Confirm")
            }

        }
    )
}

/**
 * Logout confirmation. When passes are not uploaded yet it warns that they stay on the device,
 * and offers to go upload them first.
 */
@Composable
fun LogoutConfirmDialog(
    unsyncedCount: Int,
    onLogout: () -> Unit,
    onUploadFirst: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = ShuttleTheme.colors
    AlertDialog(
        containerColor = colors.surface,
        onDismissRequest = onDismiss,
        text = {
            Text(
                text = if (unsyncedCount > 0) {
                    "$unsyncedCount shuttle pass(es) are not uploaded yet. They stay on this device " +
                        "and upload after you sign in again."
                } else {
                    "Are you sure you want to logout?"
                },
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Cancel", color = colors.primary) }
                if (unsyncedCount > 0) {
                    TextButton(onClick = onUploadFirst) { Text("Upload first", color = colors.primary) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onLogout) {
                Text(if (unsyncedCount > 0) "Log out anyway" else "Log out", color = colors.primary)
            }
        }
    )
}

@Preview
@Composable
private fun LogoutConfirmDialogPreview() {
    LogoutConfirmDialog(unsyncedCount = 3, onLogout = {}, onUploadFirst = {}, onDismiss = {})
}

@Preview
@Composable
private fun PreviewDialog() {
    ConfirmBasicDialog(
        content = "Are you sure?",
        onDismiss = {},
        onConfirm ={}
    )
}

