package com.assistant.core

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.assistant.core.adapters.DhizukuAdapter
import com.assistant.core.adapters.ShizukuAdapter
import com.assistant.core.adapters.SpecialAccessAdapter
import com.assistant.core.adapters.StandardAdapter
import com.assistant.core.engine.ActionExecutor
import com.assistant.core.engine.ActionRegistry
import com.assistant.core.engine.AssistantEngine
import com.assistant.core.engine.CapabilityBroker
import com.assistant.core.engine.CapabilityDetector
import com.assistant.core.engine.IntentClassifier
import com.assistant.core.engine.PolicyGate
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import com.assistant.core.services.AuditService
import com.assistant.core.services.CodingService
import com.assistant.core.services.FileService
import com.assistant.core.services.LocalVoiceCommand
import com.assistant.core.services.SystemService
import com.assistant.core.services.VoiceCommandParser
import com.assistant.core.services.VoiceConfig
import com.assistant.core.services.VoiceAssistantService
import com.assistant.core.services.VoicePreferences
import com.assistant.core.services.VoiceForegroundService
import com.assistant.core.services.VoiceRecognitionResult
import com.assistant.core.storage.ActionRepository
import com.assistant.core.storage.AuditRepository
import com.assistant.core.storage.Database
import com.assistant.core.storage.ProjectRepository

class MainActivity : AppCompatActivity() {

    private lateinit var commandInput: EditText
    private lateinit var outputLog: TextView

    private lateinit var actionRegistry: ActionRegistry
    private lateinit var assistantEngine: AssistantEngine
    private lateinit var auditService: AuditService
    private lateinit var systemService: SystemService
    private lateinit var capabilityState: CapabilityState
    private lateinit var voiceService: VoiceAssistantService
    private lateinit var voicePreferences: VoicePreferences
    private lateinit var voiceCommandParser: VoiceCommandParser
    private lateinit var voiceButton: Button
    private lateinit var settingsButton: Button
    private lateinit var currentVoiceConfig: VoiceConfig
    private var voiceEnabled = false
    private var shouldStartVoiceAfterPermission = false
    private var receiverRegistered = false
    private var pendingClarification: String? = null

    private val outputLines = mutableListOf<String>()
    private val voiceEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != VoiceForegroundService.ACTION_EVENT) return
            intent.getStringExtra(VoiceForegroundService.EXTRA_EVENT_MESSAGE)?.let { message ->
                appendOutput("[BG] $message")
            }
            voiceEnabled = voicePreferences.isForegroundServiceRunning()
            refreshVoiceButtonLabel()
        }
    }
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (shouldStartVoiceAfterPermission) {
                startVoiceHotwordMode()
            }
        } else {
            appendOutput(getString(R.string.voice_permission_required))
            shouldStartVoiceAfterPermission = false
        }
    }
    private val voiceSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        reloadVoiceConfiguration(showStatus = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        commandInput = findViewById(R.id.etCommandInput)
        outputLog = findViewById(R.id.tvOutputLog)
        val createButton: Button = findViewById(R.id.btnCreateProject)
        val shizukuButton: Button = findViewById(R.id.btnRunShizuku)
        val statusButton: Button = findViewById(R.id.btnShowStatus)
        voiceButton = findViewById(R.id.btnToggleVoice)
        settingsButton = findViewById(R.id.btnVoiceSettings)

        val database = Database(this)
        val projectRepository = ProjectRepository(database)
        val actionRepository = ActionRepository(database)
        val auditRepository = AuditRepository(database)

        val fileService = FileService(this)
        val codingService = CodingService(this, fileService)
        systemService = SystemService(this)
        auditService = AuditService(auditRepository)

        val standardAdapter = StandardAdapter(codingService, fileService, systemService, projectRepository)
        val shizukuAdapter = ShizukuAdapter()
        val dhizukuAdapter = DhizukuAdapter()
        val specialAccessAdapter = SpecialAccessAdapter()

        val capabilityDetector = CapabilityDetector()
        capabilityState = capabilityDetector.detect(this)

        actionRegistry = ActionRegistry()
        voiceCommandParser = VoiceCommandParser(actionRegistry)
        voicePreferences = VoicePreferences(this)
        assistantEngine = AssistantEngine(
            intentClassifier = IntentClassifier(),
            actionRegistry = actionRegistry,
            policyGate = PolicyGate(),
            actionExecutor = ActionExecutor(
                CapabilityBroker(
                    standardAdapter = standardAdapter,
                    shizukuAdapter = shizukuAdapter,
                    dhizukuAdapter = dhizukuAdapter,
                    specialAccessAdapter = specialAccessAdapter
                )
            ),
            actionRepository = actionRepository,
            auditService = auditService,
            capabilityProvider = { capabilityState }
        )

        voiceService = VoiceAssistantService(
            context = this,
            onStatus = { status -> runOnUiThread { appendOutput(status) } },
            onHotwordDetected = { runOnUiThread { appendOutput("Hotword detected: jarvis") } },
            onCommandDetected = { recognition ->
                runOnUiThread {
                    handleVoiceRecognition(recognition)
                }
            }
        )

        reloadVoiceConfiguration(showStatus = true)
        appendOutput("Startup capability status:\n${systemService.buildStatusSummary(capabilityState)}")
        appendOutput(getString(R.string.voice_hint))
        appendRecentAudit()

        createButton.setOnClickListener {
            val result = assistantEngine.executeAction(actionRegistry.createProjectRequest("assistant_demo"))
            appendActionResult(result)
            appendRecentAudit()
        }

        shizukuButton.setOnClickListener {
            val commandText = commandInput.text?.toString()?.trim().orEmpty()
            val command = if (commandText.isBlank()) "id" else commandText
            val result = assistantEngine.executeAction(
                actionRegistry.runShellRequest(command = command, confirmed = true)
            )
            appendActionResult(result)
            appendRecentAudit()
        }

        statusButton.setOnClickListener {
            val commandText = commandInput.text?.toString()?.trim().orEmpty()
            val result = if (commandText.isNotBlank()) {
                assistantEngine.handleUserCommand(commandText)
            } else {
                assistantEngine.executeAction(actionRegistry.showStatusRequest())
            }
            appendActionResult(result)
            appendRecentAudit()
        }

        voiceButton.setOnClickListener {
            if (voiceEnabled) {
                stopVoiceHotwordMode()
            } else {
                ensureMicPermissionAndStartVoice()
            }
        }

        settingsButton.setOnClickListener {
            openVoiceSettings()
        }
    }

    private fun appendActionResult(result: ActionResult) {
        val status = if (result.success) "SUCCESS" else "FAIL"
        appendOutput("[$status][${result.adapterUsed}] ${result.message}")
        result.output?.let { appendOutput(it) }
    }

    private fun appendRecentAudit() {
        val audits = auditService.getRecentEntries(limit = 5)
        if (audits.isNotEmpty()) {
            val rendered = audits.joinToString(separator = "\n") {
                "- ${it.status ?: "UNKNOWN"} | ${it.adapterUsed ?: "N/A"} | ${it.message ?: ""}"
            }
            appendOutput("Recent audit logs:\n$rendered")
        }
    }

    private fun appendOutput(text: String) {
        outputLines.add(text)
        outputLog.text = outputLines.joinToString(separator = "\n\n")
    }

    private fun handleVoiceRecognition(recognition: VoiceRecognitionResult) {
        val command = recognition.transcript
        commandInput.setText(command)
        appendOutput("Voice command (${(recognition.confidence * 100f).toInt()}%): $command")

        val awaiting = pendingClarification
        if (!awaiting.isNullOrBlank()) {
            when {
                looksLikeYes(command) -> {
                    pendingClarification = null
                    executeVoiceCommand(awaiting)
                }
                looksLikeNo(command) -> {
                    pendingClarification = null
                    appendOutput(getString(R.string.voice_clarification_cancelled))
                    voiceService.speak(getString(R.string.voice_repeat_prompt))
                }
                else -> {
                    appendOutput(getString(R.string.voice_clarification_yes_no))
                    voiceService.speak(getString(R.string.voice_clarification_yes_no))
                }
            }
            return
        }

        if (recognition.confidence < currentVoiceConfig.commandConfidenceThreshold) {
            pendingClarification = command
            val prompt = getString(R.string.voice_clarification_prompt, command)
            appendOutput(prompt)
            voiceService.speak(prompt)
            return
        }

        executeVoiceCommand(command)
    }

    private fun executeVoiceCommand(command: String) {
        val parsed = voiceCommandParser.parse(command, currentVoiceConfig)
        parsed.responseHint?.let { appendOutput(it) }

        when (parsed.localCommand) {
            LocalVoiceCommand.STOP_VOICE -> {
                stopVoiceHotwordMode()
                voiceService.speak("Voice mode disabled.")
            }
            LocalVoiceCommand.START_VOICE -> {
                ensureMicPermissionAndStartVoice()
                voiceService.speak("Voice mode enabled.")
            }
            LocalVoiceCommand.OPEN_SETTINGS -> {
                openVoiceSettings()
                voiceService.speak(getString(R.string.voice_settings_opened))
            }
            LocalVoiceCommand.NONE -> {
                val result = parsed.actionRequest?.let { assistantEngine.executeAction(it) }
                    ?: assistantEngine.handleUserCommand(parsed.fallbackTextCommand ?: command)
                appendActionResult(result)
                appendRecentAudit()
                voiceService.speak(
                    if (result.success) {
                        result.message
                    } else {
                        "I couldn't complete that command."
                    }
                )
            }
        }
    }

    private fun ensureMicPermissionAndStartVoice() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            startVoiceHotwordMode()
        } else {
            shouldStartVoiceAfterPermission = true
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceHotwordMode() {
        shouldStartVoiceAfterPermission = false
        pendingClarification = null
        if (currentVoiceConfig.useForegroundServiceMode) {
            VoiceForegroundService.start(this)
            voiceEnabled = true
            appendOutput(getString(R.string.voice_service_started))
        } else {
            voiceEnabled = true
            appendOutput(getString(R.string.voice_started))
            voiceService.startHotwordLoop()
            appendOutput(
                getString(
                    R.string.voice_engine_label,
                    voiceService.getCurrentHotwordEngine().name
                )
            )
        }
        refreshVoiceButtonLabel()
    }

    private fun stopVoiceHotwordMode() {
        pendingClarification = null
        if (currentVoiceConfig.useForegroundServiceMode) {
            VoiceForegroundService.stop(this)
            voiceEnabled = false
            appendOutput(getString(R.string.voice_service_stopped))
        } else {
            voiceEnabled = false
            voiceService.stopListening()
        }
        refreshVoiceButtonLabel()
    }

    override fun onDestroy() {
        voiceService.shutdown()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        reloadVoiceConfiguration(showStatus = false)
    }

    override fun onStart() {
        super.onStart()
        registerVoiceEventReceiver()
    }

    override fun onStop() {
        unregisterVoiceEventReceiver()
        super.onStop()
    }

    private fun openVoiceSettings() {
        voiceSettingsLauncher.launch(Intent(this, VoiceSettingsActivity::class.java))
    }

    private fun reloadVoiceConfiguration(showStatus: Boolean) {
        currentVoiceConfig = voicePreferences.load()
        voiceService.updateConfig(currentVoiceConfig)
        if (currentVoiceConfig.useForegroundServiceMode && voiceEnabled) {
            voiceService.stopListening()
        }
        voiceEnabled = if (currentVoiceConfig.useForegroundServiceMode) {
            voicePreferences.isForegroundServiceRunning()
        } else {
            voiceEnabled
        }
        if (showStatus) {
            appendOutput(
                getString(
                    R.string.voice_engine_label,
                    voiceService.getCurrentHotwordEngine().name
                )
            )
            appendOutput(
                getString(
                    R.string.voice_mode_label,
                    if (currentVoiceConfig.useForegroundServiceMode) "FOREGROUND_SERVICE" else "IN_APP"
                )
            )
        }
        refreshVoiceButtonLabel()
        if (currentVoiceConfig.useForegroundServiceMode) {
            if (currentVoiceConfig.autoStartForegroundService && !voicePreferences.isForegroundServiceRunning()) {
                ensureMicPermissionAndStartVoice()
            }
        } else if (currentVoiceConfig.autoStartVoice && !voiceEnabled) {
            ensureMicPermissionAndStartVoice()
        }
    }

    private fun refreshVoiceButtonLabel() {
        voiceButton.text = if (currentVoiceConfig.useForegroundServiceMode) {
            if (voiceEnabled) getString(R.string.stop_voice_service) else getString(R.string.start_voice_service)
        } else {
            if (voiceEnabled) getString(R.string.stop_voice_hotword) else getString(R.string.start_voice_hotword)
        }
    }

    private fun registerVoiceEventReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter(VoiceForegroundService.ACTION_EVENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(voiceEventReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(voiceEventReceiver, filter)
        }
        receiverRegistered = true
    }

    private fun unregisterVoiceEventReceiver() {
        if (!receiverRegistered) return
        unregisterReceiver(voiceEventReceiver)
        receiverRegistered = false
    }

    private fun looksLikeYes(text: String): Boolean {
        val normalized = normalize(text)
        return normalized.contains("yes") || normalized.contains("correct") || normalized.contains("confirm")
    }

    private fun looksLikeNo(text: String): Boolean {
        val normalized = normalize(text)
        return normalized.contains("no") || normalized.contains("cancel") || normalized.contains("wrong")
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
