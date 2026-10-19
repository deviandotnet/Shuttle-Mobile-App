package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.shuttleapp.ui.theme.ShuttleTheme


@Composable
fun HeaderBackground(
    leftColor: Color,
    rightColor: Color,
    modifier: Modifier = Modifier,
    height: Float = 2f
) {
    val colorList = remember(leftColor, rightColor) {
        listOf(leftColor,rightColor)
    }

    Canvas(modifier = modifier
    ) {
        drawCircle(
            radius = size.width,
            center = Offset(center.x, -size.width/height), //higher number == longer
            brush = Brush.linearGradient(colorList, end = Offset(center.x+500f,0.5f))

        )

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBackground() {
    val colors = ShuttleTheme.colors
    HeaderBackground(
        leftColor = colors.brandBand,
        rightColor = colors.brandBandDeep,
        modifier = Modifier.fillMaxSize()
    )
}