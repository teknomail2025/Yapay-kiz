package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.data.SpeakerState
import com.example.ui.theme.CallCyan
import com.example.ui.theme.CallEmerald
import com.example.ui.theme.CallTealLight
import com.example.ui.theme.WaveGlow1
import com.example.ui.theme.WaveGlow2
import com.example.ui.theme.WaveGlow3
import kotlin.math.sin

@Composable
fun AudioVisualizerWave(
    speakerState: SpeakerState,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val barCount = 28
        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(3.dp.toPx())

        val isActive = speakerState == SpeakerState.SPEAKING || speakerState == SpeakerState.LISTENING
        val baseMultiplier = when {
            speakerState == SpeakerState.SPEAKING -> 0.85f * pulse
            speakerState == SpeakerState.LISTENING -> (amplitude.coerceAtLeast(0.35f)) * pulse
            speakerState == SpeakerState.THINKING -> 0.4f * pulse
            else -> 0.15f
        }

        val colors = when (speakerState) {
            SpeakerState.LISTENING -> listOf(CallEmerald, CallTealLight)
            SpeakerState.SPEAKING -> listOf(WaveGlow1, WaveGlow2, WaveGlow3)
            SpeakerState.THINKING -> listOf(CallTealLight, CallCyan)
            else -> listOf(CallTealLight.copy(alpha = 0.3f), CallCyan.copy(alpha = 0.3f))
        }

        val brush = Brush.verticalGradient(colors = colors)

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount.toFloat()
            val sineWave = (sin(progress * 4f + phase) + 1f) / 2f
            val centerBias = 1f - kotlin.math.abs(0.5f - progress) * 1.2f

            val dynamicHeight = if (isActive || speakerState == SpeakerState.THINKING) {
                val waveHeight = (size.height * 0.25f) + (size.height * 0.7f * sineWave * baseMultiplier * centerBias)
                waveHeight.coerceIn(6.dp.toPx(), size.height)
            } else {
                (6.dp.toPx() + (sin(progress * 3f + phase) * 3.dp.toPx())).coerceAtLeast(4.dp.toPx())
            }

            val x = i * (barWidth + barSpacing)
            val y = (size.height - dynamicHeight) / 2f

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, dynamicHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
