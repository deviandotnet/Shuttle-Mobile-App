package com.example.shuttleapp.presentation.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val TrackHeight = 56.dp
private val ThumbSize = 48.dp
private val ThumbInset = 4.dp
private const val COMMIT_FRACTION = 0.9f

/**
 * A slide-to-confirm control for irreversible actions: [onComplete] runs only when the thumb is dragged
 * at least 90% of the track. A shorter drag springs back. Screen readers get a plain button action.
 */
@Composable
fun SwipeToCompleteButton(
    label: String,
    enabled: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var dragX by remember { mutableFloatStateOf(0f) }
    var committed by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) colors.successSoft else colors.surfaceSubtle)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                stateDescription = if (enabled) "Swipe right, or double tap, to confirm" else "Unavailable"
                if (enabled) onClick(label) { onComplete(); true } else disabled()
            }
    ) {
        val density = LocalDensity.current
        val insetPx = with(density) { ThumbInset.toPx() }
        val maxX = with(density) { (maxWidth - ThumbSize - ThumbInset * 2).toPx() }.coerceAtLeast(1f)
        val progress = (dragX / maxX).coerceIn(0f, 1f)

        // Mint wash that follows the thumb.
        Box(
            Modifier
                .fillMaxHeight()
                .width(with(density) { dragX.toDp() } + ThumbSize + ThumbInset * 2)
                .background(colors.success.copy(alpha = if (enabled) 0.35f else 0f))
        )
        Text(
            text = label,
            style = ShuttleType.Button,
            color = if (enabled) colors.textPrimary else colors.textDisabled,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = ThumbSize + ThumbInset)
                .alpha(1f - progress)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset { IntOffset((dragX + insetPx).roundToInt(), insetPx.roundToInt()) }
                .size(ThumbSize)
                .shadow(if (enabled) 2.dp else 0.dp, RoundedCornerShape(11.dp))
                .clip(RoundedCornerShape(11.dp))
                .background(if (enabled) colors.success else colors.border)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = enabled && !committed,
                    state = rememberDraggableState { delta -> dragX = (dragX + delta).coerceIn(0f, maxX) },
                    onDragStopped = {
                        if (dragX >= maxX * COMMIT_FRACTION) {
                            committed = true
                            animate(dragX, maxX) { value, _ -> dragX = value }
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onComplete()
                            // If the screen stays (e.g. the save failed), let the driver try again.
                            scope.launch {
                                delay(1_200)
                                animate(dragX, 0f, animationSpec = spring()) { value, _ -> dragX = value }
                                committed = false
                            }
                        } else {
                            animate(dragX, 0f, animationSpec = spring()) { value, _ -> dragX = value }
                        }
                    }
                )
        ) {
            Icon(
                Icons.Filled.KeyboardDoubleArrowRight,
                contentDescription = null,
                tint = if (enabled) colors.onSuccess else colors.surface
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun SwipeToCompleteButtonPreview() = ShuttleAppTheme(darkTheme = false) {
    Column(Modifier.background(ShuttleTheme.colors.canvas).padding(16.dp)) {
        SwipeToCompleteButton("Swipe to complete pass", enabled = true, onComplete = {})
        Spacer(Modifier.size(12.dp))
        SwipeToCompleteButton("Swipe to complete pass", enabled = false, onComplete = {})
    }
}

@Preview(widthDp = 360)
@Composable
private fun SwipeToCompleteButtonDarkPreview() = ShuttleAppTheme(darkTheme = true) {
    Column(Modifier.background(ShuttleTheme.colors.canvas).padding(16.dp)) {
        SwipeToCompleteButton("Swipe to complete pass", enabled = true, onComplete = {})
    }
}
