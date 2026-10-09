package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.CharacterEmotion
import com.example.data.ChatMessage
import com.example.data.DefaultTopics
import com.example.data.SpeakerState
import com.example.ui.theme.CallAmber
import com.example.ui.theme.CallCyan
import com.example.ui.theme.CallDarkBackground
import com.example.ui.theme.CallDarkBorder
import com.example.ui.theme.CallDarkSurface
import com.example.ui.theme.CallDarkSurfaceVariant
import com.example.ui.theme.CallEmerald
import com.example.ui.theme.CallRed
import com.example.ui.theme.CallTeal
import com.example.ui.theme.CallTealLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    viewModel: CallViewModel,
    onRequestRecordAudioPermission: () -> Unit
) {
    val persona by viewModel.selectedPersona.collectAsState()
    val emotion by viewModel.currentEmotion.collectAsState()
    val angerLevel by viewModel.angerLevel.collectAsState()
    val isSulking by viewModel.isSulking.collectAsState()
    val speakerState by viewModel.speakerState.collectAsState()
    val isVoiceEnabled by viewModel.isVoiceOutputEnabled.collectAsState()
    val voicePitch by viewModel.voicePitch.collectAsState()
    val voiceRate by viewModel.voiceRate.collectAsState()
    val audioAmplitude by viewModel.speechManager.audioAmplitude.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val floatingReaction by viewModel.floatingReaction.collectAsState()
    val micErrorMsg by viewModel.micErrorMessage.collectAsState()

    var textInputValue by remember { mutableStateOf("") }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CallDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HEADER BAR
            ChatHeader(
                personaName = persona.name,
                emotion = emotion,
                angerLevel = angerLevel,
                isVoiceEnabled = isVoiceEnabled,
                onToggleVoice = { viewModel.toggleVoiceOutput() },
                onOpenSettings = { showSettingsSheet = true }
            )

            // 2. LIVE CHARACTER STAGE (Expressions, Gestures, Reactions)
            CharacterStageSection(
                persona = persona,
                speakerState = speakerState,
                emotion = emotion,
                amplitude = audioAmplitude,
                isSulking = isSulking,
                onApologize = { viewModel.apologizeAndForgive() }
            )

            // 3. SCROLLABLE CHAT MESSAGES
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageBubble(
                        message = msg,
                        personaName = persona.name,
                        personaAvatarRes = persona.avatarRes,
                        customImageUri = persona.customImageUri,
                        onSpeak = { viewModel.speakMessage(msg.text) }
                    )
                }
            }

            // 4. QUICK SUGGESTIONS & REACTIONS ROW
            QuickTopicsRow(
                onTopicClick = { topic -> viewModel.handleUserInput(topic.prompt) },
                onReactionClick = { emoji -> viewModel.sendReaction(emoji) }
            )

            // 5. MIC ERROR ALERT IF OCCURRED
            micErrorMsg?.let { err ->
                Text(
                    text = err,
                    fontSize = 11.sp,
                    color = CallAmber,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            // 6. ALWAYS-PRESENT INPUT BAR (Voice Mic + Text Input)
            ChatInputBar(
                textValue = textInputValue,
                onTextChanged = { textInputValue = it },
                isListening = speakerState == SpeakerState.LISTENING,
                onSendText = {
                    if (textInputValue.isNotBlank()) {
                        viewModel.handleUserInput(textInputValue)
                        textInputValue = ""
                    }
                },
                onToggleMic = {
                    onRequestRecordAudioPermission()
                    if (speakerState == SpeakerState.LISTENING) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening()
                    }
                }
            )
        }

        // Floating Reaction Overlay
        floatingReaction?.let { emoji ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 48.sp)
            }
        }

        // Voice and Character Settings Sheet
        if (showSettingsSheet) {
            VoiceSettingsDialog(
                sheetState = settingsSheetState,
                currentPersona = persona,
                voicePitch = voicePitch,
                voiceRate = voiceRate,
                angerLevel = angerLevel,
                isVideoMode = true,
                onUpdateName = { newName -> viewModel.updateCharacterName(newName) },
                onSelectAvatarRes = { resId -> viewModel.updateCharacterAvatarRes(resId) },
                onSelectCustomImage = { uri -> viewModel.updateCustomImageUri(uri) },
                onApplyVoicePreset = { preset -> viewModel.applyVoicePreset(preset) },
                onChangePitch = { viewModel.setVoicePitch(it) },
                onChangeRate = { viewModel.setVoiceRate(it) },
                onToggleVideoMode = { /* keep interactive mode */ },
                onDismiss = { showSettingsSheet = false }
            )
        }
    }
}

@Composable
private fun ChatHeader(
    personaName: String,
    emotion: CharacterEmotion,
    angerLevel: Int,
    isVoiceEnabled: Boolean,
    onToggleVoice: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = CallDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CallDarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (angerLevel >= 70) CallRed else CallEmerald)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = personaName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = when {
                            emotion == CharacterEmotion.HANGUP || angerLevel >= 70 -> "😡 Küstü & Trip Atıyor"
                            emotion == CharacterEmotion.ANNOYED || angerLevel >= 35 -> "🙄 Hafif Tripli"
                            emotion == CharacterEmotion.LAUGHING -> "😂 Gülüyor"
                            else -> "✨ Canlı Sohbet Arkadaşı"
                        },
                        fontSize = 11.sp,
                        color = when {
                            angerLevel >= 70 -> CallRed
                            angerLevel >= 35 -> CallAmber
                            else -> CallCyan
                        }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Voice Output Toggle
                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier.testTag("toggle_voice_output_button")
                ) {
                    Icon(
                        imageVector = if (isVoiceEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Sesli Okuma",
                        tint = if (isVoiceEnabled) CallCyan else TextMuted
                    )
                }

                // Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Ayarlar",
                        tint = TextWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterStageSection(
    persona: com.example.data.CharacterPersona,
    speakerState: SpeakerState,
    emotion: CharacterEmotion,
    amplitude: Float,
    isSulking: Boolean,
    onApologize: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Character Stage Box with live expressions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 2.dp,
                    color = if (isSulking) CallRed else CallDarkBorder,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            CharacterView(
                persona = persona,
                speakerState = speakerState,
                emotion = emotion,
                amplitude = amplitude,
                isVideoCall = true,
                modifier = Modifier.fillMaxSize()
            )

            // Sulking / Trip Alert overlay
            if (isSulking) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CallRed.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { onApologize() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Gönlünü Al",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Küstü! Gönlünü Almak İçin Dokun ❤️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Waveform Visualizer
        AudioVisualizerWave(
            speakerState = speakerState,
            amplitude = amplitude,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    personaName: String,
    personaAvatarRes: Int,
    customImageUri: String?,
    onSpeak: () -> Unit
) {
    val isUser = message.sender == "user"
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            // AI avatar thumbnail
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, CallCyan, CircleShape)
            ) {
                if (!customImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(customImageUri).crossfade(true).build(),
                        contentDescription = personaName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = personaAvatarRes),
                        contentDescription = personaName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) CallTeal else CallDarkSurfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUser) "Sen" else personaName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) Color.White.copy(alpha = 0.85f) else CallCyan
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isUser) {
                        // Play voice button on character messages
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Seslendir",
                                tint = CallCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = if (isUser) Color.White.copy(alpha = 0.7f) else TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = TextWhite,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun QuickTopicsRow(
    onTopicClick: (com.example.data.QuickTopic) -> Unit,
    onReactionClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp)) {
        // Quick reaction emojis
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("❤️", "👏", "😊", "😂", "😡", "💡").forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onReactionClick(emoji) }
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Topic chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.testTag("quick_topics_row")
        ) {
            items(DefaultTopics.list) { topic ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CallDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CallDarkBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTopicClick(topic) }
                ) {
                    Text(
                        text = topic.title,
                        fontSize = 11.sp,
                        color = CallCyan,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    textValue: String,
    onTextChanged: (String) -> Unit,
    isListening: Boolean,
    onSendText: () -> Unit,
    onToggleMic: () -> Unit
) {
    Surface(
        color = CallDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CallDarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big Glowing Mic Button (Tap to Talk)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) Brush.linearGradient(listOf(CallEmerald, CallTealLight))
                        else Brush.linearGradient(listOf(CallTeal, CallCyan))
                    )
                    .border(
                        width = if (isListening) 2.dp else 0.dp,
                        color = if (isListening) CallEmerald else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onToggleMic() }
                    .testTag("mic_input_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Durdur" else "Sesle Konuş",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input Field
            OutlinedTextField(
                value = textValue,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Dinliyorum, konuş…" else "Bir şeyler yaz veya sesle söyle…",
                        color = if (isListening) CallEmerald else TextMuted,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isListening) CallEmerald else CallCyan,
                    unfocusedBorderColor = CallDarkBorder,
                    focusedContainerColor = CallDarkBackground,
                    unfocusedContainerColor = CallDarkBackground,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = onSendText,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (textValue.isNotBlank()) CallTeal else CallDarkSurfaceVariant)
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Gönder",
                    tint = if (textValue.isNotBlank()) Color.White else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
