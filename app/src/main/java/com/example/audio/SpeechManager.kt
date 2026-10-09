package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false
    private var pendingSpeechText: String? = null

    private var currentPitch = 1.15f
    private var currentRate = 1.04f

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("SpeechManager", "TTS initialization failed: ${e.message}")
        }
        setupRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val turkishLocale = Locale.forLanguageTag("tr-TR")
            val result = tts?.setLanguage(turkishLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.forLanguageTag("tr"))
            }
            tts?.setPitch(currentPitch)
            tts?.setSpeechRate(currentRate)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _audioAmplitude.value = 0.75f
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioAmplitude.value = 0.0f
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioAmplitude.value = 0.0f
                }
            })
            isTtsReady = true

            // Speak queued speech if any
            pendingSpeechText?.let {
                speak(it)
                pendingSpeechText = null
            }
        } else {
            Log.e("SpeechManager", "TTS init status was not success: $status")
        }
    }

    fun setVoicePitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
        if (isTtsReady) {
            tts?.setPitch(currentPitch)
        }
    }

    fun setVoiceRate(rate: Float) {
        currentRate = rate.coerceIn(0.5f, 2.0f)
        if (isTtsReady) {
            tts?.setSpeechRate(currentRate)
        }
    }

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    private fun setupRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("SpeechManager", "Speech recognition not available on this device")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _audioAmplitude.value = 0.8f
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val norm = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1f)
                        _audioAmplitude.value = norm
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioAmplitude.value = 0.1f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioAmplitude.value = 0.0f
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "Ses tam anlaşılamadı"
                            SpeechRecognizer.ERROR_NETWORK -> "Ağ bağlantısı gerekiyor"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Ses algılanamadı"
                            SpeechRecognizer.ERROR_AUDIO -> "Ses kaydı hatası"
                            else -> "Mikrofon yanıt vermedi ($error)"
                        }
                        onSpeechError?.invoke(errorMsg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioAmplitude.value = 0.0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0]
                            onSpeechRecognized?.invoke(recognized)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _audioAmplitude.value = 0.6f
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.e("SpeechManager", "Error initializing speech recognizer: ${e.message}")
        }
    }

    fun speak(text: String, utteranceId: String = "ai_utterance_${System.currentTimeMillis()}") {
        if (!isTtsReady || tts == null) {
            pendingSpeechText = text
            Log.w("SpeechManager", "TTS is not ready yet, queuing text: $text")
            return
        }
        stopSpeaking()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        _audioAmplitude.value = 0.0f
    }

    fun startListening() {
        stopSpeaking()
        if (speechRecognizer == null) {
            setupRecognizer()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
            onSpeechError?.invoke("Mikrofon başlatılamadı: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _audioAmplitude.value = 0.0f
    }

    fun setSpeakerphone(enable: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        audioManager?.let {
            @Suppress("DEPRECATION")
            it.isSpeakerphoneOn = enable
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("SpeechManager", "Shutdown error: ${e.message}")
        }
    }
}
