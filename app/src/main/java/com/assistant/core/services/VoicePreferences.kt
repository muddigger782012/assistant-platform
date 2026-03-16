package com.assistant.core.services

import android.content.Context

data class VoiceConfig(
    val enableDedicatedWakeWord: Boolean,
    val porcupineAccessKey: String,
    val wakeSensitivity: Float,
    val autoStartVoice: Boolean,
    val preferOfflineCommandRecognition: Boolean
)

class VoicePreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): VoiceConfig {
        return VoiceConfig(
            enableDedicatedWakeWord = preferences.getBoolean(KEY_DEDICATED_WAKE_WORD, true),
            porcupineAccessKey = preferences.getString(KEY_PORCUPINE_ACCESS_KEY, "").orEmpty().trim(),
            wakeSensitivity = preferences.getFloat(KEY_WAKE_SENSITIVITY, 0.6f),
            autoStartVoice = preferences.getBoolean(KEY_AUTO_START, false),
            preferOfflineCommandRecognition = preferences.getBoolean(KEY_PREFER_OFFLINE_COMMAND, true)
        )
    }

    fun save(config: VoiceConfig) {
        preferences.edit()
            .putBoolean(KEY_DEDICATED_WAKE_WORD, config.enableDedicatedWakeWord)
            .putString(KEY_PORCUPINE_ACCESS_KEY, config.porcupineAccessKey.trim())
            .putFloat(KEY_WAKE_SENSITIVITY, config.wakeSensitivity.coerceIn(0.1f, 1.0f))
            .putBoolean(KEY_AUTO_START, config.autoStartVoice)
            .putBoolean(KEY_PREFER_OFFLINE_COMMAND, config.preferOfflineCommandRecognition)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "voice_settings"
        private const val KEY_DEDICATED_WAKE_WORD = "dedicated_wake_word"
        private const val KEY_PORCUPINE_ACCESS_KEY = "porcupine_access_key"
        private const val KEY_WAKE_SENSITIVITY = "wake_sensitivity"
        private const val KEY_AUTO_START = "auto_start_voice"
        private const val KEY_PREFER_OFFLINE_COMMAND = "prefer_offline_command_recognition"
    }
}
