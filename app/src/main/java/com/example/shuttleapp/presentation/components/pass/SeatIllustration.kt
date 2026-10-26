package com.example.shuttleapp.presentation.components.pass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import com.example.shuttleapp.ui.theme.ShuttleTheme

/** Design box the drawing is authored in (matches the 06 mockup); it scales to the width it is given. */
private const val BOX_W = 188f
private const val BOX_H = 132f

/** Empty-manifest illustration: an empty shuttle seat with a mint "add" badge. Decorative. */
@Composable
fun SeatIllustration(modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Canvas(modifier.aspectRatio(BOX_W / BOX_H)) {
        val s = minOf(size.width / BOX_W, size.height / BOX_H)
        translate(left = (size.width - BOX_W * s) / 2, top = 0f) {
            scale(s, pivot = Offset.Zero) {
                // Ground and clouds
                drawOval(colors.surfaceQuiet, Offset(14f, 104f), Size(160f, 24f))
                val cloud = colors.surfaceQuiet.copy(alpha = 0.6f)
                listOf(Offset(30f, 40f) to 9f, Offset(42f, 36f) to 12f, Offset(54f, 41f) to 8f,
                    Offset(146f, 28f) to 8f, Offset(157f, 24f) to 11f, Offset(168f, 29f) to 7f)
                    .forEach { (c, r) -> drawCircle(cloud, r, c) }
                // Seat back, headrest and cushion
                drawRoundRect(colors.border, Offset(70f, 22f), Size(30f, 66f), CornerRadius(10f))
                drawRoundRect(colors.surfaceQuiet, Offset(76f, 14f), Size(22f, 16f), CornerRadius(6f))
                drawRoundRect(colors.surfaceQuiet, Offset(70f, 78f), Size(62f, 16f), CornerRadius(8f))
                // Frame and pedestal
                drawPath(
                    Path().apply {
                        moveTo(66f, 46f); lineTo(66f, 82f)
                        quadraticTo(66f, 96f, 80f, 96f); lineTo(124f, 96f)
                    },
                    colors.primary,
                    style = Stroke(width = 7f, cap = StrokeCap.Round)
                )
                drawRect(colors.primary, Offset(96f, 96f), Size(7f, 16f))
                drawRoundRect(colors.primary, Offset(82f, 110f), Size(36f, 5f), CornerRadius(2.5f))
                // Mint add badge
                drawCircle(colors.success, 17f, Offset(134f, 46f))
                drawLine(colors.onSuccess, Offset(134f, 38f), Offset(134f, 54f), 3.2f, StrokeCap.Round)
                drawLine(colors.onSuccess, Offset(126f, 46f), Offset(142f, 46f), 3.2f, StrokeCap.Round)
            }
        }
    }
}

@Preview(widthDp = 188, heightDp = 132, showBackground = true)
@Composable
private fun SeatIllustrationPreview() {
    SeatIllustration()
}
