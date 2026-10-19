package com.example.shuttleapp.presentation.components.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import com.example.shuttleapp.ui.theme.ShuttleColors
import com.example.shuttleapp.ui.theme.ShuttleTheme

/** Design box the drawing is authored in; it scales to whatever size it is given. */
private const val BOX_W = 300f
private const val BOX_H = 170f

/** Decorative city scene with a Midnight bus on a curved road. Scales with its width. */
@Composable
fun BusIllustration(modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Canvas(modifier.aspectRatio(BOX_W / BOX_H)) {
        val s = minOf(size.width / BOX_W, size.height / BOX_H)
        translate(left = size.width - BOX_W * s, top = size.height - BOX_H * s) {
            scale(s, pivot = Offset.Zero) {
                drawSkyline(colors)
                drawRoad(colors)
                drawBus(colors)
                drawPin(colors, Offset(272f, 62f))
            }
        }
    }
}

private fun DrawScope.drawSkyline(colors: ShuttleColors) {
    val building = colors.illoBuilding
    listOf(
        Triple(150f, 26f, 58f), Triple(178f, 20f, 88f), Triple(200f, 30f, 70f),
        Triple(232f, 22f, 96f), Triple(256f, 30f, 64f), Triple(284f, 16f, 80f)
    ).forEach { (x, w, h) ->
        drawRect(building, topLeft = Offset(x, 128f - h), size = Size(w, h))
    }
    val cloud = colors.surfaceQuiet
    drawRoundRect(cloud, Offset(132f, 16f), Size(34f, 10f), CornerRadius(5f))
    drawCircle(cloud, 8f, Offset(148f, 15f))
    drawRoundRect(cloud, Offset(240f, 22f), Size(40f, 11f), CornerRadius(6f))
    drawCircle(cloud, 9f, Offset(258f, 21f))
}

private fun DrawScope.drawRoad(colors: ShuttleColors) {
    drawPath(Path().apply {
        moveTo(0f, BOX_H); lineTo(0f, 150f)
        cubicTo(110f, 128f, 210f, 128f, BOX_W, 104f)
        lineTo(BOX_W, BOX_H); close()
    }, colors.surfaceQuiet)
    drawPath(Path().apply {
        moveTo(20f, BOX_H)
        cubicTo(120f, 150f, 210f, 146f, BOX_W, 124f)
        lineTo(BOX_W, BOX_H); close()
    }, colors.illoRoad)
    drawPath(Path().apply {
        moveTo(60f, BOX_H - 4f)
        cubicTo(140f, 156f, 220f, 154f, BOX_W, 140f)
    }, colors.illoRoadMarking, style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))))
}

private fun DrawScope.drawBus(colors: ShuttleColors) {
    // Ground shadow
    drawOval(colors.busShadow, Offset(78f, 140f), Size(180f, 12f))
    // Body, skirt and roof
    drawRoundRect(colors.busBody, Offset(72f, 62f), Size(180f, 76f), CornerRadius(14f))
    drawRoundRect(colors.busTrim, Offset(72f, 118f), Size(180f, 20f), CornerRadius(10f))
    drawRoundRect(colors.busTrim, Offset(86f, 56f), Size(140f, 8f), CornerRadius(4f))
    // Side windows
    val glass = colors.busGlass.copy(alpha = 0.92f)
    for (i in 0 until 4) {
        drawRoundRect(glass, Offset(84f + i * 30f, 72f), Size(24f, 26f), CornerRadius(4f))
    }
    // Door and windscreen
    drawRoundRect(colors.busGlass.copy(alpha = 0.55f), Offset(206f, 72f), Size(18f, 52f), CornerRadius(3f))
    drawLine(colors.busBody, Offset(215f, 74f), Offset(215f, 122f), strokeWidth = 1.5f)
    drawRoundRect(glass, Offset(230f, 70f), Size(17f, 36f), CornerRadius(4f))
    // colors.success brand stripe, lights
    drawRect(colors.success, Offset(72f, 106f), Size(132f, 5f))
    drawCircle(colors.busLight, 4f, Offset(246f, 122f))
    drawRoundRect(colors.error, Offset(72f, 112f), Size(4f, 10f), CornerRadius(2f))
    // Wheels
    listOf(108f, 214f).forEach { x ->
        drawCircle(colors.busBody, 13f, Offset(x, 138f))
        drawCircle(colors.busTrim, 13f, Offset(x, 138f), style = Stroke(width = 3f))
        drawCircle(colors.busGlass, 5f, Offset(x, 138f))
    }
}

private fun DrawScope.drawPin(colors: ShuttleColors, tip: Offset) {
    val head = Offset(tip.x, tip.y - 22f)
    drawPath(Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(head.x - 8f, head.y + 4f)
        lineTo(head.x + 8f, head.y + 4f)
        close()
    }, colors.success)
    drawCircle(colors.success, 10f, head)
    drawCircle(colors.highlight, 4f, head)
}


@Preview(widthDp = 300, heightDp = 170, showBackground = true)
@Composable
private fun BusIllustrationPreview() {
    BusIllustration()
}
