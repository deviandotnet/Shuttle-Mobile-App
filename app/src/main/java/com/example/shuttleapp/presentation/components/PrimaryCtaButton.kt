package com.example.shuttleapp.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** Full-width Midnight call to action from DESIGN-SYSTEM.md (12dp radius, 52dp min height). */
@Composable
fun PrimaryCtaButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit = {}
) {
    val colors = ShuttleTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = Dimens.CtaHeight),
        shape = RoundedCornerShape(Dimens.RadiusButton),
        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
        contentPadding = PaddingValues(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS + Dimens.SpaceXs)
    ) {
        leading()
        Spacer(Modifier.width(Dimens.SpaceS + Dimens.SpaceXs))
        Text(label, style = ShuttleType.Button, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Filled plus-in-circle, for "create" actions. */
@Composable
fun CtaPlusIcon() {
    Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(24.dp))
}

/** Pulsing mint dot that marks something in progress. */
@Composable
fun LiveDot() {
    val colors = ShuttleTheme.colors
    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "liveAlpha"
    )
    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(18.dp).alpha(pulse).clip(CircleShape).background(colors.success.copy(alpha = 0.4f)))
        Box(Modifier.size(10.dp).clip(CircleShape).background(colors.success))
    }
}

@Preview(widthDp = 360)
@Composable
private fun PrimaryCtaButtonPreview() {
    Column(Modifier.padding(16.dp)) {
        PrimaryCtaButton("New shuttle pass", onClick = {}, leading = { CtaPlusIcon() })
        Spacer(Modifier.size(12.dp))
        PrimaryCtaButton("View current trip", onClick = {}, leading = { LiveDot() })
    }
}
