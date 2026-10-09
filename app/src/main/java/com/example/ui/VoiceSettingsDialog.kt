package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CharacterPersona
import com.example.data.PresetAvatar
import com.example.data.PresetAvatars
import com.example.data.VoicePreset
import com.example.data.VoicePresets
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsDialog(
    sheetState: SheetState,
    currentPersona: CharacterPersona,
    voicePitch: Float,
    voiceRate: Float,
    angerLevel: Int,
    isVideoMode: Boolean,
    onUpdateName: (String) -> Unit,
    onSelectAvatarRes: (Int) -> Unit,
    onSelectCustomImage: (String) -> Unit,
    onApplyVoicePreset: (VoicePreset) -> Unit,
    onChangePitch: (Float) -> Unit,
    onChangeRate: (Float) -> Unit,
    onToggleVideoMode: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var editNameText by remember { mutableStateOf(currentPersona.name) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSelectCustomImage(uri.toString())
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CallDarkBackground,
        dragHandle = null,
        modifier = Modifier.testTag("voice_settings_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Karakter, Resim & Ses Ayarları",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "İsim, görsel, kadınsı sesler ve canlı kapris modu",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = TextWhite
                        )
                    }
                }
            }

            // 1. İSİM DEĞİŞTİRME
            item {
                Text(
                    text = "Karakterin İsmini Değiştir",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CallTealLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { editNameText = it },
                        placeholder = { Text("Örn: Aylin, Leyla, Aslı...", color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("character_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CallCyan,
                            unfocusedBorderColor = CallDarkBorder,
                            focusedContainerColor = CallDarkSurface,
                            unfocusedContainerColor = CallDarkSurface,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onUpdateName(editNameText) },
                        colors = ButtonDefaults.buttonColors(containerColor = CallTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_name_button")
                    ) {
                        Text("Kaydet", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // 2. GÖRSEL / RESİM DEĞİŞTİRME
            item {
                Text(
                    text = "Görseldeki Resmi Değiştir",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CallTealLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Presets row + Gallery button
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(PresetAvatars.list) { preset ->
                        val isSelected = currentPersona.avatarRes == preset.resId && currentPersona.customImageUri == null
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectAvatarRes(preset.resId) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) CallCyan else CallDarkBorder,
                                        shape = CircleShape
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = preset.resId),
                                    contentDescription = preset.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.name.split(" ")[0],
                                fontSize = 11.sp,
                                color = if (isSelected) CallCyan else TextMuted
                            )
                        }
                    }

                    // Galeriden Seç Butonu
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(CallDarkSurfaceVariant)
                                    .border(1.dp, CallCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Galeriden Seç",
                                    tint = CallCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Galeriden",
                                fontSize = 11.sp,
                                color = CallCyan
                            )
                        }
                    }
                }
            }

            // 3. YENİ KADINSI SESLER
            item {
                Text(
                    text = "Ses Karakteri (Yeni Kadınsı Sesler)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CallTealLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VoicePresets.list.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CallDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CallDarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onApplyVoicePreset(preset) }
                                .testTag("voice_preset_${preset.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = preset.description,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = "${String.format("%.2f", preset.pitch)}x",
                                    fontSize = 11.sp,
                                    color = CallCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 4. KIZMA & SABIR DURUMU (TRİP / KAPARİS SAYACI)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CallDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CallDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Ruh Hali",
                                    tint = if (angerLevel > 40) CallRed else CallEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ruh Hali & Sabır Durumu",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                            Text(
                                text = when {
                                    angerLevel >= 75 -> "Çok Kızgın! (Kapatabilir)"
                                    angerLevel >= 35 -> "Tripli & Kaprisli"
                                    else -> "Sakin & Neşeli"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    angerLevel >= 75 -> CallRed
                                    angerLevel >= 35 -> CallAmber
                                    else -> CallEmerald
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kaba veya kırıcı konuşulursa trip atar, çok kızarsa telefonu yüzüne kapatır! Özür dilersen barışır.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // 5. GÖRÜŞME MODU (GÖRÜNTÜLÜ vs SESLİ)
            item {
                Text(
                    text = "Görünüm Modu",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CallTealLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeChoiceButton(
                        title = "Görüntülü Sohbet",
                        subtitle = "Tam ekran mimik ve jestler",
                        isSelected = isVideoMode,
                        onClick = { onToggleVideoMode(true) },
                        modifier = Modifier.weight(1f)
                    )
                    ModeChoiceButton(
                        title = "Sesli Telefon",
                        subtitle = "Klasik ses dalgalı arayüz",
                        isSelected = !isVideoMode,
                        onClick = { onToggleVideoMode(false) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 6. MANUEL SES TONU VE HIZI
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Ses Perdesi (İnce/Kalın)", fontSize = 12.sp, color = TextWhite)
                    Text(text = String.format("%.2fx", voicePitch), fontSize = 11.sp, color = CallCyan)
                }
                Slider(
                    value = voicePitch,
                    onValueChange = onChangePitch,
                    valueRange = 0.8f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = CallCyan, activeTrackColor = CallTealLight)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Konuşma Hızı", fontSize = 12.sp, color = TextWhite)
                    Text(text = String.format("%.2fx", voiceRate), fontSize = 11.sp, color = CallCyan)
                }
                Slider(
                    value = voiceRate,
                    onValueChange = onChangeRate,
                    valueRange = 0.8f..1.3f,
                    colors = SliderDefaults.colors(thumbColor = CallCyan, activeTrackColor = CallTealLight)
                )
            }

            item {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CallTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_and_close_button")
                ) {
                    Text("Değişiklikleri Uygula", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ModeChoiceButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) CallTeal.copy(alpha = 0.25f) else CallDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) CallCyan else CallDarkBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) CallCyan else TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = TextMuted)
        }
    }
}
