package com.example.shuttleapp.presentation.components.scanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.state.ScanResultUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

private val WindowTop = 82.dp
private val MaxWindow = 240.dp
private val MinWindow = 160.dp
/** Room kept under the window for the result card and the sheet that overlaps the feed. */
private val BelowWindow = 122.dp
private val ResultGap = 22.dp
private val WindowRadius = 20.dp

/**
 * The camera feed under a dimmed scrim with a square scan window (mockups 14–14c). The window's brackets
 * take the result's colour, and the result card sits just below it. [feed] receives the window in px.
 */
@Composable
fun ScannerViewfinder(
    result: ScanResultUi?,
    modifier: Modifier = Modifier,
    feed: @Composable (scanWindow: Rect) -> Unit
) {
    val colors = ShuttleTheme.colors
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxSize().background(colors.brandBand)) {
        val windowSize = (maxHeight - WindowTop - BelowWindow).coerceIn(MinWindow, MaxWindow)
        val window = with(density) {
            Rect(Offset(((maxWidth - windowSize) / 2).toPx(), WindowTop.toPx()), Size(windowSize.toPx(), windowSize.toPx()))
        }
        val stateColor = when (result) {
            null -> colors.onBrandBand.copy(alpha = 0.92f)
            is ScanResultUi.Boarded -> colors.success
            is ScanResultUi.AlreadyOnBoard -> colors.warning
            ScanResultUi.NotPassenger -> colors.error
        }

        feed(window)
        Canvas(Modifier.fillMaxSize()) { drawFrame(window, colors.brandBand.copy(alpha = 0.62f), stateColor, outlined = result != null) }
        ScannerHint(Modifier.align(Alignment.TopCenter).padding(top = Dimens.SpaceL, start = Dimens.SpaceL, end = Dimens.SpaceL))
        result?.let {
            ScanResultCard(
                result = it,
                modifier = Modifier.padding(top = WindowTop + windowSize + ResultGap, start = Dimens.SpaceM, end = Dimens.SpaceM)
            )
        }
    }
}

@Composable
private fun ScannerHint(modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Hold the passenger's QR in the frame",
            style = ShuttleType.Body.copy(fontWeight = FontWeight.Medium),
            color = colors.onBrandBand,
            textAlign = TextAlign.Center
        )
        Text(
            "It scans on its own",
            style = ShuttleType.Label.copy(fontWeight = FontWeight.Normal),
            color = colors.onBrandBand.copy(alpha = 0.76f),
            textAlign = TextAlign.Center
        )
    }
}

private fun DrawScope.drawFrame(window: Rect, scrim: Color, color: Color, outlined: Boolean) {
    val radius = CornerRadius(WindowRadius.toPx())
    val hole = Path().apply { addRoundRect(RoundRect(window, radius)) }
    clipPath(hole, ClipOp.Difference) { drawRect(scrim) }
    if (outlined) {
        val inset = 1.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = window.topLeft + Offset(inset, inset),
            size = Size(window.width - inset * 2, window.height - inset * 2),
            cornerRadius = radius,
            style = Stroke(2.dp.toPx())
        )
    }
    drawBrackets(window.inflate(1.dp.toPx()), color)
}

/** Four 40dp corner brackets with rounded corners, drawn just outside the window edge. */
private fun DrawScope.drawBrackets(frame: Rect, color: Color) {
    val stroke = 4.dp.toPx()
    val length = 40.dp.toPx()
    val r = 22.dp.toPx()
    val arc = Size(r * 2, r * 2)
    val style = Stroke(stroke, cap = StrokeCap.Round)
    val (l, t, rt, b) = listOf(frame.left, frame.top, frame.right, frame.bottom)
    // top-left, top-right, bottom-right, bottom-left
    drawArc(color, 180f, 90f, false, Offset(l, t), arc, style = style)
    drawArc(color, 270f, 90f, false, Offset(rt - 2 * r, t), arc, style = style)
    drawArc(color, 0f, 90f, false, Offset(rt - 2 * r, b - 2 * r), arc, style = style)
    drawArc(color, 90f, 90f, false, Offset(l, b - 2 * r), arc, style = style)
    listOf(
        Offset(l, t + r) to Offset(l, t + length), Offset(l + r, t) to Offset(l + length, t),
        Offset(rt - r, t) to Offset(rt - length, t), Offset(rt, t + r) to Offset(rt, t + length),
        Offset(rt, b - r) to Offset(rt, b - length), Offset(rt - r, b) to Offset(rt - length, b),
        Offset(l + r, b) to Offset(l + length, b), Offset(l, b - r) to Offset(l, b - length)
    ).forEach { (from, to) -> drawLine(color, from, to, stroke, StrokeCap.Round) }
}
