package com.assistant.core.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAssistantService(
    private val context: Context,
    private val onStatus: (String) -> Unit,
    private val onHotwordDetected: () -> Unit,
    private val onCommandDetected: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private enum class VoiceMode { HOTWORD, COMMAND }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var mode: VoiceMode = VoiceMode.HOTWORD
    private var manuallyStopped = true
    private var listening = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var ttsReady = false

    fun isRecognitionAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startHotwordLoop() {
        if (!isRecognitionAvailable()) {
            onStatus("Speech recognition is unavailable on this device.")
            return
        }
        manuallyStopped = false
        mode = VoiceMode.HOTWORD
        scheduleListening(100)
    }

    fun stopListening() {
        manuallyStopped = true
        listening = false
        mainHandler.removeCallbacksAndMessages(null)
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        onStatus("Voice listener stopped.")
    }

    fun shutdown() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts.stop()
        tts.shutdown()
    }

    fun speak(text: String) {
        if (ttsReady) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "jarvis-response")
        }
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            tts.language = Locale.US
        }
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        listening = false
    }

    override fun onError(error: Int) {
        listening = false
        if (manuallyStopped) return
        scheduleListening(600)
    }

    override fun onResults(results: Bundle?) {
        listening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
        handleMatches(matches)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        if (mode != VoiceMode.HOTWORD) return
        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
        if (containsJarvis(partial)) {
            speechRecognizer?.cancel()
            listening = false
            onHotwordDetected()
            speak("Yes?")
            mode = VoiceMode.COMMAND
            scheduleListening(350)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private fun handleMatches(matches: List<String>) {
        if (manuallyStopped) return

        when (mode) {
            VoiceMode.HOTWORD -> {
                if (containsJarvis(matches)) {
                    onHotwordDetected()
                    speak("Yes?")
                    mode = VoiceMode.COMMAND
                    scheduleListening(350)
                } else {
                    scheduleListening(300)
                }
            }
            VoiceMode.COMMAND -> {
                val command = matches.firstOrNull { it.isNotBlank() }?.trim()
                if (command.isNullOrBlank()) {
                    onStatus("No command detected after hotword.")
                } else {
                    onCommandDetected(command)
                }
                mode = VoiceMode.HOTWORD
                scheduleListening(400)
            }
        }
    }

    private fun containsJarvis(phrases: List<String>): Boolean {
        return phrases.any { it.lowercase(Locale.US).contains("jarvis") }
    }

    private fun getOrCreateRecognizer(): SpeechRecognizer {
        val current = speechRecognizer
        if (current != null) return current
        return SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(this)
            speechRecognizer = it
        }
    }

    private fun scheduleListening(delayMs: Long) {
        if (manuallyStopped) return
        mainHandler.postDelayed({ beginListening() }, delayMs)
    }

    private fun beginListening() {
        if (manuallyStopped || listening) return
        val recognizer = getOrCreateRecognizer()
        val prompt = if (mode == VoiceMode.HOTWORD) {
            "Say jarvis"
        } else {
            "Listening for command"
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        }
        listening = true
        recognizer.startListening(intent)
        if (mode == VoiceMode.HOTWORD) {
            onStatus("Listening for hotword: jarvis")
        } else {
            onStatus("Hotword detected. Listening for command...")
        }
    }
}
