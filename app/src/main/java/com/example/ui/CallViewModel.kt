package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechManager
import com.example.data.CallMode
import com.example.data.CharacterEmotion
import com.example.data.CharacterPersona
import com.example.data.ChatMessage
import com.example.data.ConversationalEngine
import com.example.data.PersonaRepository
import com.example.data.SpeakerState
import com.example.data.VoicePreset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallViewModel(application: Application) : AndroidViewModel(application) {

    val speechManager = SpeechManager(application.applicationContext)
    private val engine = ConversationalEngine()

    private val _selectedPersona = MutableStateFlow(PersonaRepository.deniz)
    val selectedPersona: StateFlow<CharacterPersona> = _selectedPersona.asStateFlow()

    private val _currentEmotion = MutableStateFlow(CharacterEmotion.HAPPY)
    val currentEmotion: StateFlow<CharacterEmotion> = _currentEmotion.asStateFlow()

    private val _angerLevel = MutableStateFlow(0) // 0 to 100
    val angerLevel: StateFlow<Int> = _angerLevel.asStateFlow()

    private val _isSulking = MutableStateFlow(false) // Trip atıyor / küstü modu
    val isSulking: StateFlow<Boolean> = _isSulking.asStateFlow()

    private val _isVoiceOutputEnabled = MutableStateFlow(true)
    val isVoiceOutputEnabled: StateFlow<Boolean> = _isVoiceOutputEnabled.asStateFlow()

    private val _voicePitch = MutableStateFlow(1.15f)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceRate = MutableStateFlow(1.04f)
    val voiceRate: StateFlow<Float> = _voiceRate.asStateFlow()

    private val _speakerState = MutableStateFlow(SpeakerState.IDLE)
    val speakerState: StateFlow<SpeakerState> = _speakerState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _floatingReaction = MutableStateFlow<String?>(null)
    val floatingReaction: StateFlow<String?> = _floatingReaction.asStateFlow()

    private val _micErrorMessage = MutableStateFlow<String?>(null)
    val micErrorMessage: StateFlow<String?> = _micErrorMessage.asStateFlow()

    init {
        speechManager.setVoicePitch(_voicePitch.value)
        speechManager.setVoiceRate(_voiceRate.value)

        // Observe TTS state
        viewModelScope.launch {
            speechManager.isSpeaking.collect { isSpeaking ->
                if (isSpeaking) {
                    _speakerState.value = SpeakerState.SPEAKING
                } else if (_speakerState.value == SpeakerState.SPEAKING) {
                    _speakerState.value = SpeakerState.IDLE
                }
            }
        }

        // Observe Speech Recognizer state
        viewModelScope.launch {
            speechManager.isListening.collect { isListening ->
                if (isListening) {
                    _speakerState.value = SpeakerState.LISTENING
                } else if (_speakerState.value == SpeakerState.LISTENING) {
                    _speakerState.value = SpeakerState.IDLE
                }
            }
        }

        speechManager.onSpeechRecognized = { text ->
            _micErrorMessage.value = null
            handleUserInput(text)
        }

        speechManager.onSpeechError = { errorMsg ->
            _speakerState.value = SpeakerState.IDLE
            _micErrorMessage.value = errorMsg
            viewModelScope.launch {
                delay(3500)
                _micErrorMessage.value = null
            }
        }

        // DIRECT INITIAL GREETING ON APP STARTUP
        viewModelScope.launch {
            delay(500)
            val greeting = _selectedPersona.value.greetingText
            postAiMessage(greeting, CharacterEmotion.HAPPY)
        }
    }

    fun setPersona(persona: CharacterPersona) {
        _selectedPersona.value = persona
        _voicePitch.value = persona.defaultPitch
        _voiceRate.value = persona.defaultRate
        speechManager.setVoicePitch(persona.defaultPitch)
        speechManager.setVoiceRate(persona.defaultRate)
        _angerLevel.value = 0
        _isSulking.value = false
        val newGreeting = "Merhaba! Artık seninle ${persona.name} olarak konuşuyorum. Nasıl yardımcı olabilirim?"
        postAiMessage(newGreeting, CharacterEmotion.HAPPY)
    }

    fun updateCharacterName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            _selectedPersona.value = _selectedPersona.value.copy(name = trimmed)
            postAiMessage("Adımı $trimmed olarak güncelledin, çok sevdim! Dinliyorum seni.", CharacterEmotion.HAPPY)
        }
    }

    fun updateCharacterAvatarRes(resId: Int) {
        _selectedPersona.value = _selectedPersona.value.copy(avatarRes = resId, customImageUri = null)
    }

    fun updateCustomImageUri(uriString: String) {
        _selectedPersona.value = _selectedPersona.value.copy(customImageUri = uriString)
    }

    fun applyVoicePreset(preset: VoicePreset) {
        _voicePitch.value = preset.pitch
        _voiceRate.value = preset.rate
        speechManager.setVoicePitch(preset.pitch)
        speechManager.setVoiceRate(preset.rate)
        postAiMessage("Ses tonumu ${preset.name} olarak ayarladım. Sesim nasıl geliyor?", CharacterEmotion.TALKING)
    }

    fun setVoicePitch(pitch: Float) {
        _voicePitch.value = pitch
        speechManager.setVoicePitch(pitch)
    }

    fun setVoiceRate(rate: Float) {
        _voiceRate.value = rate
        speechManager.setVoiceRate(rate)
    }

    fun toggleVoiceOutput() {
        val next = !_isVoiceOutputEnabled.value
        _isVoiceOutputEnabled.value = next
        if (!next) {
            speechManager.stopSpeaking()
        }
    }

    fun sendReaction(emoji: String) {
        _floatingReaction.value = emoji
        viewModelScope.launch {
            delay(1800)
            _floatingReaction.value = null
        }
    }

    fun startListening() {
        _micErrorMessage.value = null
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun speakMessage(text: String) {
        speechManager.speak(text)
    }

    fun apologizeAndForgive() {
        _angerLevel.value = 0
        _isSulking.value = false
        val forgiveReply = "Hım... Madem içten bir şekilde özür diledin, hemen affediyorum seni! Gel sarılalım, bir daha kalbimi kırmak yok ama tamam mı?"
        postAiMessage(forgiveReply, CharacterEmotion.HAPPY)
    }

    fun handleUserInput(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val userMsg = ChatMessage(sender = "user", text = trimmed)
        _messages.value = _messages.value + userMsg
        _speakerState.value = SpeakerState.THINKING
        _currentEmotion.value = CharacterEmotion.THINKING

        viewModelScope.launch {
            delay(300)
            val result = engine.getResponse(trimmed, _messages.value, _selectedPersona.value, _angerLevel.value)

            val updatedAnger = (_angerLevel.value + result.angerDelta).coerceIn(0, 100)
            _angerLevel.value = updatedAnger

            if (result.shouldHangUp || updatedAnger >= 75) {
                _isSulking.value = true
                _currentEmotion.value = CharacterEmotion.ANGRY
            } else if (result.angerDelta < 0) {
                _isSulking.value = false
                _currentEmotion.value = CharacterEmotion.HAPPY
            } else {
                _currentEmotion.value = result.emotion
            }

            postAiMessage(result.spokenText, _currentEmotion.value)
        }
    }

    private fun postAiMessage(reply: String, emotion: CharacterEmotion) {
        val aiMsg = ChatMessage(sender = "ai", text = reply, emotion = emotion)
        _messages.value = _messages.value + aiMsg
        if (_isVoiceOutputEnabled.value) {
            speechManager.speak(reply)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
