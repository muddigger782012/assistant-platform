package com.assistant.core

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.assistant.core.services.VoiceConfig
import com.assistant.core.services.VoicePreferences
import com.google.android.material.switchmaterial.SwitchMaterial

class VoiceSettingsActivity : AppCompatActivity() {

    private lateinit var voicePreferences: VoicePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.voice_settings_title)

        voicePreferences = VoicePreferences(this)
        val current = voicePreferences.load()

        val dedicatedSwitch: SwitchMaterial = findViewById(R.id.switchDedicatedWakeWord)
        val autoStartSwitch: SwitchMaterial = findViewById(R.id.switchAutoStartVoice)
        val offlineSwitch: SwitchMaterial = findViewById(R.id.switchPreferOfflineCommand)
        val accessKeyInput: EditText = findViewById(R.id.etPorcupineAccessKey)
        val sensitivitySeek: SeekBar = findViewById(R.id.seekWakeSensitivity)
        val sensitivityValue: TextView = findViewById(R.id.tvWakeSensitivityValue)
        val saveButton: Button = findViewById(R.id.btnSaveVoiceSettings)

        dedicatedSwitch.isChecked = current.enableDedicatedWakeWord
        autoStartSwitch.isChecked = current.autoStartVoice
        offlineSwitch.isChecked = current.preferOfflineCommandRecognition
        accessKeyInput.setText(current.porcupineAccessKey)
        sensitivitySeek.progress = (current.wakeSensitivity * 100f).toInt()
        sensitivityValue.text = formatSensitivity(current.wakeSensitivity)

        sensitivitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = (progress.coerceIn(10, 100)) / 100f
                sensitivityValue.text = formatSensitivity(value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        saveButton.setOnClickListener {
            val config = VoiceConfig(
                enableDedicatedWakeWord = dedicatedSwitch.isChecked,
                porcupineAccessKey = accessKeyInput.text?.toString().orEmpty(),
                wakeSensitivity = (sensitivitySeek.progress.coerceIn(10, 100)) / 100f,
                autoStartVoice = autoStartSwitch.isChecked,
                preferOfflineCommandRecognition = offlineSwitch.isChecked
            )
            voicePreferences.save(config)
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun formatSensitivity(value: Float): String {
        return String.format("Sensitivity: %.2f", value)
    }
}
