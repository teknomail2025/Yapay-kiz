package com.example.data

import com.example.R

enum class CallState {
    IDLE,
    RINGING,
    CONNECTED,
    ENDED
}

enum class SpeakerState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

enum class CallMode {
    AUDIO_CALL,
    VIDEO_CALL
}

enum class CharacterEmotion {
    NEUTRAL,
    TALKING,
    LISTENING,
    THINKING,
    CURIOUS,
    HAPPY,
    LAUGHING,
    ANNOYED,
    ANGRY,
    HANGUP
}

data class VoicePreset(
    val id: String,
    val name: String,
    val description: String,
    val pitch: Float,
    val rate: Float
)

object VoicePresets {
    val list = listOf(
        VoicePreset("playful", "Cilveli & Neşeli", "Tatlı, canlı ve kadınsı ses tonu", 1.18f, 1.05f),
        VoicePreset("warm", "Sıcak & Doğal", "Samimi, huzurlu ve akıcı kadın sesi", 1.06f, 1.00f),
        VoicePreset("sweet", "Tatlı & İnce", "Genç ve melodik kadınsı ses", 1.25f, 1.02f),
        VoicePreset("capricious", "Kaprisli & Tripli", "Hafif cilveli ve hazırcevap ton", 1.12f, 1.08f),
        VoicePreset("male_friendly", "Erkek / Can", "Dostane ve enerjik erkek sesi", 0.88f, 1.00f)
    )
}

data class CharacterPersona(
    val id: String,
    var name: String,
    val title: String,
    var avatarRes: Int,
    var customImageUri: String? = null,
    val speakingRes: Int,
    val listeningRes: Int,
    val thinkingRes: Int,
    val angryRes: Int = R.drawable.deniz_angry,
    val laughingRes: Int = R.drawable.deniz_laughing,
    val defaultPitch: Float,
    val defaultRate: Float,
    val greetingText: String,
    val personalityPrompt: String
)

data class PresetAvatar(
    val id: String,
    val name: String,
    val resId: Int
)

object PresetAvatars {
    val list = listOf(
        PresetAvatar("deniz_default", "Deniz (Klasik)", R.drawable.friend_avatar),
        PresetAvatar("aylin", "Aylin (Şık & Esmer)", R.drawable.aylin_avatar),
        PresetAvatar("deniz_listen", "Deniz (Samimi)", R.drawable.deniz_listening),
        PresetAvatar("can", "Can (Erkek Dost)", R.drawable.can_avatar)
    )
}

object PersonaRepository {
    val deniz = CharacterPersona(
        id = "deniz",
        name = "Deniz",
        title = "Samimi, Cilveli ve Neşeli",
        avatarRes = R.drawable.friend_avatar,
        speakingRes = R.drawable.deniz_speaking,
        listeningRes = R.drawable.deniz_listening,
        thinkingRes = R.drawable.deniz_thinking,
        angryRes = R.drawable.deniz_angry,
        laughingRes = R.drawable.deniz_laughing,
        defaultPitch = 1.15f,
        defaultRate = 1.04f,
        greetingText = "Alo, selam! Sesini duymak ne güzel, tam da böyle rahat bir sohbet havası arıyordum. Günün nasıl geçiyor, her şey yolunda mı?",
        personalityPrompt = "Sen neşeli, bazen tatlı kaprisler yapan, esprili ve canlı mimikli bir genç kadın arkadaşsın."
    )

    val aylin = CharacterPersona(
        id = "aylin",
        name = "Aylin",
        title = "Şık, Hazırcevap ve Sıcak",
        avatarRes = R.drawable.aylin_avatar,
        speakingRes = R.drawable.deniz_speaking,
        listeningRes = R.drawable.deniz_listening,
        thinkingRes = R.drawable.deniz_thinking,
        angryRes = R.drawable.deniz_angry,
        laughingRes = R.drawable.deniz_laughing,
        defaultPitch = 1.12f,
        defaultRate = 1.02f,
        greetingText = "Selam canım! Ben de kahvemi yudumluyordum tam. Sesini duymak çok iyi geldi, neler yapıyorsun bakalım?",
        personalityPrompt = "Sen Aylin'sin; şık, zeki, tatlı dilli ve samimi bir arkadaşsın."
    )

    val can = CharacterPersona(
        id = "can",
        name = "Can",
        title = "Enerjik ve Esprili Dost",
        avatarRes = R.drawable.can_avatar,
        speakingRes = R.drawable.can_avatar,
        listeningRes = R.drawable.can_avatar,
        thinkingRes = R.drawable.can_avatar,
        angryRes = R.drawable.can_avatar,
        laughingRes = R.drawable.can_avatar,
        defaultPitch = 0.88f,
        defaultRate = 1.00f,
        greetingText = "Selam dostum! Arayıp sesini duyurduğun ne iyi oldu ya. Neler yapıyorsun, keyifler nasıl?",
        personalityPrompt = "Sen Can'sın; enerjik, esprili ve motive edici konuşan yakın bir arkadaşsın."
    )

    val all = listOf(deniz, aylin, can)
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = true,
    val emotion: CharacterEmotion = CharacterEmotion.NEUTRAL
)

data class QuickTopic(
    val title: String,
    val prompt: String
)

object DefaultTopics {
    val list = listOf(
        QuickTopic("Espri yap", "Bana çok komik bir espri yapsana, biraz gülelim!"),
        QuickTopic("İnternetten ara", "Bana internetten güncel bir bilgi veya ilginç bir haber anlatsana?"),
        QuickTopic("Günün nasıl geçti?", "Günün nasıl geçti, neler yaptın bugün?"),
        QuickTopic("Biraz dertleşelim", "Bugün biraz yoruldum, seninle dertleşmek istedim."),
        QuickTopic("Trip atma!", "Ya hemen kapris yapma bana, şaka yapıyorum sadece!"),
        QuickTopic("Özür dilerim", "Gerçekten özür dilerim, kırdıysam affet lütfen barışalım mı?"),
        QuickTopic("Hafta sonu planı", "Hafta sonu için bana ne önerirsin?")
    )
}
