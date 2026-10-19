package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.state.ReauthState
import com.example.shuttleapp.ui.theme.NavyBlue
import com.example.shuttleapp.ui.theme.White

/** Sign in again to refresh an expired token. Nothing on the device is cleared. */
@Composable
fun ReauthDialog(
    state: ReauthState,
    onSubmit: (username: String, password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf(state.username) }
    var password by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        containerColor = White,
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        title = { Text("Sign in to upload") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your passes are saved on this device. Sign in again to upload them.")
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    enabled = !state.isSubmitting,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            if (state.isSubmitting) {
                CircularProgressIndicator(color = NavyBlue)
            } else {
                TextButton(onClick = { onSubmit(username.trim(), password) }) { Text("Sign in", color = NavyBlue) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isSubmitting) { Text("Later", color = NavyBlue) }
        }
    )
}

@Preview
@Composable
private fun ReauthDialogPreview() {
    ReauthDialog(state = ReauthState(username = "jdelacruz", error = "Sign in failed."), onSubmit = { _, _ -> }, onDismiss = {})
}
