package com.example.shuttleapp.presentation.components.scanner

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.shuttleapp.presentation.events.ScanFeedback

private const val TONE_VOLUME = 80
private const val TONE_MS = 150

/**
 * Haptic + short tone per scan outcome, so the driver (or a passenger at the door) knows the result
 * without looking at the screen.
 */
@Composable
fun rememberScanFeedback(): (ScanFeedback) -> Unit {
    val haptics = LocalHapticFeedback.current
    val tone = remember {
        try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, TONE_VOLUME)
        } catch (ex: RuntimeException) {
            // Some devices refuse a tone generator (audio busy, policy); haptics still work.
            Log.w("ScanFeedback", "Tone generator unavailable", ex)
            null
        }
    }
    DisposableEffect(tone) { onDispose { tone?.release() } }
    return remember(haptics, tone) {
        { kind ->
            when (kind) {
                ScanFeedback.Success -> {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    tone?.startTone(ToneGenerator.TONE_PROP_ACK, TONE_MS)
                }
                ScanFeedback.Warning -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    tone?.startTone(ToneGenerator.TONE_PROP_BEEP2, TONE_MS)
                }
                ScanFeedback.Error -> {
                    haptics.performHapticFeedback(HapticFeedbackType.Reject)
                    tone?.startTone(ToneGenerator.TONE_SUP_ERROR, TONE_MS * 2)
                }
            }
        }
    }
}
