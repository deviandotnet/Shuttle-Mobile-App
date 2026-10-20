package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.ShuttleTheme

/** Decorative city skyline, road and pin at the foot of the drawer. Scales to its width. */
@Composable
fun DrawerSkyline(modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Canvas(
        modifier
            .fillMaxWidth()
            .height(120.dp)
            .clearAndSetSemantics {}
    ) {
        val w = size.width
        val h = size.height
        val ground = h * 0.72f
        // (x, width, height) as fractions of the canvas.
        val buildings = listOf(
            Triple(0.04f, 0.08f, 0.30f), Triple(0.13f, 0.10f, 0.52f), Triple(0.24f, 0.07f, 0.36f),
            Triple(0.33f, 0.12f, 0.62f), Triple(0.47f, 0.08f, 0.40f), Triple(0.57f, 0.11f, 0.56f),
            Triple(0.70f, 0.07f, 0.34f), Triple(0.79f, 0.10f, 0.48f), Triple(0.90f, 0.08f, 0.28f)
        )
        buildings.forEachIndexed { index, (x, bw, bh) ->
            drawRoundRect(
                color = if (index % 2 == 0) colors.brandBandLine else colors.brandAccent.copy(alpha = 0.35f),
                topLeft = Offset(w * x, ground - h * bh),
                size = Size(w * bw, h * bh),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }
        val road = Path().apply {
            moveTo(0f, h * 0.95f)
            cubicTo(w * 0.35f, h * 0.70f, w * 0.55f, h * 1.05f, w, h * 0.80f)
        }
        drawPath(road, colors.illoRoad, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
        drawPath(road, colors.onBrandBandMuted.copy(alpha = 0.35f), style = Stroke(width = 1.dp.toPx()))

        val pin = Offset(w * 0.38f, h * 0.62f)
        val r = 7.dp.toPx()
        drawPath(Path().apply {
            moveTo(pin.x - r * 0.8f, pin.y + r * 0.4f)
            lineTo(pin.x, pin.y + r * 2.0f)
            lineTo(pin.x + r * 0.8f, pin.y + r * 0.4f)
            close()
        }, colors.success)
        drawCircle(colors.success, radius = r, center = pin)
        drawCircle(colors.brandBand, radius = r * 0.4f, center = pin)
    }
}
