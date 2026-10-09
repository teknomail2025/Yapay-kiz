package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.CharacterEmotion
import com.example.data.CharacterPersona
import com.example.data.SpeakerState
import com.example.ui.theme.CallCyan
import com.example.ui.theme.CallDarkBorder
import com.example.ui.theme.CallEmerald
import com.example.ui.theme.CallRed
import com.example.ui.theme.CallTealLight
import com.example.ui.theme.WaveGlow1
import com.example.ui.theme.WaveGlow2

@Composable
fun CharacterView(
    persona: CharacterPersona,
    speakerState: SpeakerState,
    emotion: CharacterEmotion,
    amplitude: Float,
    isVideoCall: Boolean,
    modifier: Modifier = Modifier,
    circularSize: Dp = 150.dp
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "char_motion")

    // Organic breathing motion (more energetic or agitated depending on anger)
    val isAngry = emotion == CharacterEmotion.ANGRY || emotion == CharacterEmotion.ANNOYED || emotion == CharacterEmotion.HANGUP
    val isLaughing = emotion == CharacterEmotion.LAUGHING

    val breathScale by infiniteTransition.animateFloat(
        initialValue = if (isLaughing) 0.97f else 0.99f,
        targetValue = if (isLaughing) 1.03f else 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isLaughing) 350 else 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Speech gesture head sway & sulk head tilt
    val headSway by infiniteTransition.animateFloat(
        initialValue = if (isAngry) -2.5f else -1.2f,
        targetValue = if (isAngry) 2.5f else 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (speakerState == SpeakerState.SPEAKING) 750 else 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val headBob by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isLaughing) 6f else if (speakerState == SpeakerState.SPEAKING) 3f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isLaughing) 200 else if (speakerState == SpeakerState.SPEAKING) 380 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura"
    )

    // Active expression selection
    val activeDrawableRes = when {
        isAngry -> persona.angryRes
        isLaughing -> persona.laughingRes
        speakerState == SpeakerState.SPEAKING -> persona.speakingRes
        speakerState == SpeakerState.LISTENING -> persona.listeningRes
        speakerState == SpeakerState.THINKING -> persona.thinkingRes
        emotion == CharacterEmotion.HAPPY -> persona.laughingRes
        else -> persona.avatarRes
    }

    if (isVideoCall) {
        // FULL VIDEO CALL PRESENTATION
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .border(
                    2.dp,
                    if (isAngry) CallRed else CallDarkBorder,
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = Pair(activeDrawableRes, persona.customImageUri),
                animationSpec = tween(350),
                label = "expression_crossfade"
            ) { (drawableId, customUri) ->
                if (!customUri.isNullOrBlank() && !isAngry && !isLaughing && speakerState == SpeakerState.IDLE) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(customUri).crossfade(true).build(),
                        contentDescription = "${persona.name} Özel Fotoğraf",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(breathScale)
                            .rotate(headSway)
                            .offset(y = headBob.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = "${persona.name} Canlı Görünüm",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(breathScale + (if (speakerState == SpeakerState.SPEAKING) amplitude * 0.05f else 0f))
                            .rotate(headSway)
                            .offset(y = headBob.dp)
                    )
                }
            }

            // Vignette gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // Dynamic Emotion / Mimic Indicator Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isAngry -> CallRed.copy(alpha = 0.75f)
                    isLaughing -> Color(0xFFF59E0B).copy(alpha = 0.75f)
                    else -> Color.Black.copy(alpha = 0.6f)
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            ) {
                val mimicDesc = when {
                    emotion == CharacterEmotion.HANGUP -> "😡 Çok Kızdı, Kapatıyor!"
                    emotion == CharacterEmotion.ANGRY -> "😤 Öfkeli (Trip Atıyor)"
                    emotion == CharacterEmotion.ANNOYED -> "🙄 Alındı & Kaprisli"
                    isLaughing -> "😂 Kahkaha Atıyor"
                    speakerState == SpeakerState.SPEAKING -> "🗣️ Konuşuyor (Canlı Mimik)"
                    speakerState == SpeakerState.LISTENING -> "👂 Dikkatle Dinliyor"
                    speakerState == SpeakerState.THINKING -> "🤔 Düşünüyor / Araştırıyor"
                    else -> "✨ Canlı Sohbet"
                }
                Text(
                    text = mimicDesc,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    } else {
        // CIRCULAR PHONE CALL AVATAR
        Box(
            modifier = modifier.size(circularSize + 40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (speakerState == SpeakerState.SPEAKING || speakerState == SpeakerState.LISTENING || isAngry) {
                Box(
                    modifier = Modifier
                        .size(circularSize + 30.dp)
                        .scale(auraScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isAngry) CallRed.copy(alpha = 0.45f)
                                    else if (speakerState == SpeakerState.SPEAKING) WaveGlow1.copy(alpha = 0.4f)
                                    else CallEmerald.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Box(
                modifier = Modifier
                    .size(circularSize)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = if (isAngry) listOf(CallRed, Color(0xFFDC2626))
                            else if (speakerState == SpeakerState.SPEAKING) listOf(WaveGlow1, WaveGlow2)
                            else listOf(CallTealLight, CallCyan)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = Pair(activeDrawableRes, persona.customImageUri),
                    animationSpec = tween(300),
                    label = "circular_expression_crossfade"
                ) { (drawableId, customUri) ->
                    if (!customUri.isNullOrBlank() && !isAngry && !isLaughing && speakerState == SpeakerState.IDLE) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(customUri).crossfade(true).build(),
                            contentDescription = "${persona.name} Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(breathScale)
                                .rotate(headSway * 0.7f)
                                .offset(y = headBob.dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "${persona.name} Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(breathScale)
                                .rotate(headSway * 0.7f)
                                .offset(y = headBob.dp)
                        )
                    }
                }
            }
        }
    }
}
