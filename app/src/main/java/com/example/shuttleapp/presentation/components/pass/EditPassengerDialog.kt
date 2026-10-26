package com.example.shuttleapp.presentation.components.pass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.components.ModifiedTextField
import com.example.shuttleapp.presentation.state.EditPassengerUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** Corrects a mis-scanned QR code. Validation runs in the ViewModel; its message shows under the field. */
@Composable
fun EditPassengerDialog(
    editing: EditPassengerUi,
    onQrChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = ShuttleTheme.colors
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = colors.surface,
        shape = RoundedCornerShape(Dimens.RadiusCard),
        title = { Text("Edit passenger", style = ShuttleType.Heading2, color = colors.textPrimary) },
        text = {
            Column {
                ModifiedTextField(
                    label = "Passenger QR code",
                    errorIndicator = editing.error != null,
                    value = editing.qrCode,
                    onValueChanged = onQrChanged
                )
                editing.error?.let {
                    Text(it, style = ShuttleType.Caption, color = colors.error, modifier = Modifier.padding(top = 6.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, colors = ButtonDefaults.textButtonColors(contentColor = colors.primary)) {
                Text("Save", style = ShuttleType.Button)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = colors.textSecondary)) {
                Text("Cancel", style = ShuttleType.Button)
            }
        }
    )
}
