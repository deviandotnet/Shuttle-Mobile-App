package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.state.PassUploadState
import com.example.shuttleapp.presentation.state.SyncStatus
import com.example.shuttleapp.ui.theme.ShuttleTheme

/** Progress, result and sign-in-again dialogs for an upload of all pending passes. */
@Composable
fun UploadDialogs(
    upload: PassUploadState,
    onDismissResult: () -> Unit,
    onSubmitReauth: (username: String, password: String) -> Unit,
    onDismissReauth: () -> Unit
) {
    SyncResultDialog(upload.sync, onDismissResult)
    upload.reauth?.let { reauth ->
        ReauthDialog(state = reauth, onSubmit = onSubmitReauth, onDismiss = onDismissReauth)
    }
}

@Composable
fun SyncResultDialog(sync: SyncStatus, onDismiss: () -> Unit) {
    val colors = ShuttleTheme.colors
    when (sync) {
        SyncStatus.Idle -> Unit
        SyncStatus.Running -> LoadingDialog()
        is SyncStatus.Success -> InformationTextDialog(
            onDismissRequest = onDismiss,
            dialogTitle = "Success!",
            dialogText = sync.message,
            icon = Icons.TwoTone.CheckCircle,
            iconTint = colors.success
        )
        is SyncStatus.Failed -> InformationTextDialog(
            onDismissRequest = onDismiss,
            dialogTitle = "Error!",
            dialogText = sync.message,
            icon = Icons.TwoTone.Cancel,
            iconTint = colors.error
        )
    }
}

/** Offline or signed-out notice above lists that upload. Shows nothing while uploads can go through. */
@Composable
fun ConnectionBanner(isOnline: Boolean, needsSignIn: Boolean, onSignIn: () -> Unit) {
    val colors = ShuttleTheme.colors
    if (!isOnline) {
        StatusBanner(
            text = "Offline — passes are saved on this device",
            background = colors.warningSoft,
            color = colors.warningText
        )
    } else if (needsSignIn) {
        StatusBanner(
            text = "Sign in again to upload. Tap here.",
            background = colors.errorSoft,
            color = colors.error,
            onClick = onSignIn
        )
    }
}

@Composable
private fun StatusBanner(text: String, background: Color, color: Color, onClick: (() -> Unit)? = null) {
    Text(
        text = text,
        color = color,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
