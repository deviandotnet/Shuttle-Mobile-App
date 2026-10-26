package com.example.shuttleapp.presentation.components.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuttleapp.presentation.events.QrScannerAction
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** Shown in place of the camera when permission is denied (mockup 14d). The manifest sheet stays below it. */
@Composable
fun CameraAccessOff(onAction: (QrScannerAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(colors.brandBand)
            // Navy: the brand blue over the band, so it follows the dark theme too.
            .background(colors.brandAccent.copy(alpha = 0.4f))
            .verticalScroll(rememberScrollState())
            .padding(start = Dimens.SpaceXl, end = Dimens.SpaceXl, top = 52.dp, bottom = 40.dp)
    ) {
        Box(Modifier.size(120.dp).clip(CircleShape).background(colors.onBrandBand.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.VideocamOff, contentDescription = null, tint = colors.onBrandBandMuted, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(Dimens.SpaceL))
        Text(
            "Camera access is off",
            style = ShuttleType.Section.copy(fontSize = 22.sp, lineHeight = 30.sp),
            color = colors.onBrandBand,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Dimens.SpaceS))
        Text(
            "Scanning needs the camera. Passengers already on board stay on this pass.",
            style = ShuttleType.Body,
            color = colors.onBrandBand.copy(alpha = 0.78f),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp)
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onAction(QrScannerAction.RequestCamera) },
            shape = RoundedCornerShape(Dimens.RadiusButton),
            colors = ButtonDefaults.buttonColors(containerColor = colors.success, contentColor = colors.onSuccess),
            modifier = Modifier.width(220.dp).height(Dimens.CtaHeight)
        ) {
            Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text("Allow camera", style = ShuttleType.Button)
        }
        Spacer(Modifier.height(6.dp))
        TextButton(onClick = { onAction(QrScannerAction.OpenAppSettings) }, modifier = Modifier.height(Dimens.MinTouch)) {
            Text("Open app settings", style = ShuttleType.Button.copy(fontSize = 15.sp), color = colors.onBrandBand)
        }
    }
}
