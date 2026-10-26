package com.example.shuttleapp.presentation.components.pass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.components.SwipeToCompleteButton
import com.example.shuttleapp.presentation.state.PassActionUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** Bottom actions for an open draft: scan passengers, then start the trip, then swipe to finish it. */
@Composable
fun PassActionBar(
    action: PassActionUi,
    onAddPassenger: () -> Unit,
    onStartDeparture: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    if (action == PassActionUi.None) return
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS + Dimens.SpaceXs)
    ) {
        OutlinedButton(
            onClick = onAddPassenger,
            modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.CtaHeight),
            shape = RoundedCornerShape(Dimens.RadiusButton),
            border = BorderStroke(1.5.dp, colors.primary),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.surface, contentColor = colors.primary)
        ) {
            ButtonContent(Icons.Filled.QrCodeScanner, "Add passenger")
        }
        when (action) {
            PassActionUi.StartDeparture -> Button(
                onClick = onStartDeparture,
                modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.CtaHeight),
                shape = RoundedCornerShape(Dimens.RadiusButton),
                colors = ButtonDefaults.buttonColors(containerColor = colors.success, contentColor = colors.onSuccess),
                contentPadding = PaddingValues(horizontal = Dimens.SpaceL)
            ) {
                ButtonContent(Icons.Filled.PlayArrow, "Start departure")
            }
            is PassActionUi.Complete -> {
                SwipeToCompleteButton("Swipe to complete pass", enabled = action.enabled, onComplete = onComplete)
                action.hint?.let {
                    Text(it, style = ShuttleType.Caption, color = colors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
            PassActionUi.None -> Unit
        }
    }
}

@Composable
private fun ButtonContent(icon: ImageVector, label: String) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
    Spacer(Modifier.width(10.dp))
    Text(label, style = ShuttleType.Button)
}
