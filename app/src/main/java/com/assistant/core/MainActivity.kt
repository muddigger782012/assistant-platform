package com.assistant.core

import android.Manifest
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.ClipData
import android.content.ClipboardManager
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.UserManager
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
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
import com.assistant.core.services.DhizukuService
import com.assistant.core.services.DhizukuStatus
import com.assistant.core.services.FileService
import com.assistant.core.services.HybridAssistantService
import com.assistant.core.services.LocalVoiceCommand
import com.assistant.core.services.PrivilegeCatalogService
import com.assistant.core.services.RunningShizukuCommand
import com.assistant.core.services.ShizukuShellService
import com.assistant.core.services.SystemService
import com.assistant.core.services.VoiceAssistantService
import com.assistant.core.services.VoiceCommandParser
import com.assistant.core.services.VoiceConfig
import com.assistant.core.services.VoiceForegroundService
import com.assistant.core.services.VoicePreferences
import com.assistant.core.services.VoiceRecognitionResult
import com.assistant.core.storage.ActionRepository
import com.assistant.core.storage.AuditRepository
import com.assistant.core.storage.Database
import com.assistant.core.storage.ProjectRepository
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import com.rosan.dhizuku.api.Dhizuku
import rikka.shizuku.Shizuku
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var commandInput: EditText
    private lateinit var shizukuCommandInput: EditText
    private lateinit var terminalCommandInput: EditText
    private lateinit var shizukuRuntimeStatusView: TextView
    private lateinit var outputLog: TextView
    private lateinit var statusOutput: TextView
    private lateinit var specialPermissionsStatus: TextView
    private lateinit var terminalOutputView: TextView
    private lateinit var terminalHistoryView: TextView
    private lateinit var auditDebugOutput: TextView
    private lateinit var micPermissionStatusView: TextView
    private lateinit var grantMicPermissionButton: Button
    private lateinit var privilegeCenterView: TextView
    private lateinit var refreshPrivilegesButton: Button
    private lateinit var requestDhizukuPermissionButton: Button
    private lateinit var applyCoreDelegatedScopesButton: Button
    private lateinit var terminalRunButton: Button
    private lateinit var terminalStopButton: Button
    private lateinit var terminalClearButton: Button
    private lateinit var refreshShizukuStatusButton: Button
    private lateinit var openShizukuAppButton: Button
    private lateinit var requestShizukuPermissionButton: Button
    private lateinit var auditRefreshButton: Button
    private lateinit var auditClearViewButton: Button
    private lateinit var auditCopyButton: Button
    private lateinit var auditLimitInput: EditText
    private lateinit var auditStatusFilterInput: EditText
    private lateinit var auditAdapterFilterInput: EditText

    private lateinit var voiceButton: Button
    private lateinit var settingsButton: Button

    private lateinit var switchCameraDisabled: SwitchMaterial
    private lateinit var switchScreenCaptureDisabled: SwitchMaterial
    private lateinit var switchInstallAppsRestricted: SwitchMaterial
    private lateinit var switchUninstallAppsRestricted: SwitchMaterial
    private lateinit var switchStatusBarDisabled: SwitchMaterial
    private lateinit var switchPackageUninstallBlocked: SwitchMaterial
    private lateinit var lockScreenMessageInput: EditText
    private lateinit var policyPackageNameInput: EditText

    private lateinit var btnApplyCameraDisabled: Button
    private lateinit var btnApplyScreenCaptureDisabled: Button
    private lateinit var btnApplyInstallAppsRestriction: Button
    private lateinit var btnApplyUninstallAppsRestriction: Button
    private lateinit var btnApplyStatusBarDisabled: Button
    private lateinit var btnApplyLockScreenMessage: Button
    private lateinit var btnApplyPackagePolicy: Button
    private lateinit var btnLockNow: Button
    private lateinit var btnRebootFromDhizuku: Button

    private lateinit var tabLayout: TabLayout
    private lateinit var tabSections: List<View>

    private lateinit var actionRegistry: ActionRegistry
    private lateinit var assistantEngine: AssistantEngine
    private lateinit var auditService: AuditService
    private lateinit var systemService: SystemService
    private lateinit var capabilityState: CapabilityState
    private lateinit var voiceService: VoiceAssistantService
    private lateinit var voicePreferences: VoicePreferences
    private lateinit var voiceCommandParser: VoiceCommandParser
    private lateinit var hybridAssistantService: HybridAssistantService
    private lateinit var dhizukuService: DhizukuService
    private lateinit var privilegeCatalogService: PrivilegeCatalogService
    private lateinit var shizukuShellService: ShizukuShellService
    private lateinit var currentVoiceConfig: VoiceConfig
    private lateinit var currentDhizukuStatus: DhizukuStatus

    private val outputLines = mutableListOf<String>()
    private val terminalOutputLines = mutableListOf<String>()
    private val terminalHistory = mutableListOf<String>()
    private val uiPreferences by lazy { getSharedPreferences("main_ui", MODE_PRIVATE) }
    private val devicePolicyManager by lazy {
        getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    private var voiceEnabled = false
    private var shouldStartVoiceAfterPermission = false
    private var receiverRegistered = false
    private var shizukuListenersRegistered = false
    private var pendingClarification: String? = null
    private var runningTerminalCommand: RunningShizukuCommand? = null

    private val shizukuBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        runOnUiThread {
            appendOutput("Shizuku binder connected.")
            refreshShizukuRuntimeStatus()
        }
    }

    private val shizukuBinderDeadListener = Shizuku.OnBinderDeadListener {
        runOnUiThread {
            appendOutput("Shizuku binder disconnected.")
            refreshShizukuRuntimeStatus()
        }
    }

    private val shizukuPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
            runOnUiThread {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    appendOutput("Shizuku permission granted.")
                } else {
                    appendOutput("Shizuku permission denied.")
                }
                refreshShizukuRuntimeStatus()
            }
        }
    }

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
        updateMicrophonePermissionUi()
        if (granted) {
            if (shouldStartVoiceAfterPermission) startVoiceHotwordMode()
        } else {
            appendOutput(getString(R.string.voice_permission_required))
            shouldStartVoiceAfterPermission = false
        }
    }

    private val runtimePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults ->
        val granted = grantResults.filterValues { it }.keys
        val denied = grantResults.filterValues { !it }.keys
        appendOutput("Runtime granted: ${if (granted.isEmpty()) "none" else granted.joinToString()}")
        appendOutput("Runtime denied: ${if (denied.isEmpty()) "none" else denied.joinToString()}")
        refreshSpecialPermissionsStatus()
    }

    private val voiceSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        reloadVoiceConfiguration(showStatus = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupTabs()

        val database = Database(this)
        val projectRepository = ProjectRepository(database)
        val actionRepository = ActionRepository(database)
        val auditRepository = AuditRepository(database)

        val fileService = FileService(this)
        val codingService = CodingService(this, fileService)
        systemService = SystemService(this)
        auditService = AuditService(auditRepository)

        val standardAdapter = StandardAdapter(codingService, fileService, systemService, projectRepository)
        val shizukuAdapter = ShizukuAdapter(this)
        dhizukuService = DhizukuService()
        privilegeCatalogService = PrivilegeCatalogService()
        shizukuShellService = ShizukuShellService(this)
        val dhizukuAdapter = DhizukuAdapter(this, dhizukuService)
        val specialAccessAdapter = SpecialAccessAdapter()

        capabilityState = CapabilityDetector().detect(this)

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
        hybridAssistantService = HybridAssistantService(
            assistantEngine = assistantEngine,
            actionRegistry = actionRegistry,
            systemService = systemService,
            capabilityProvider = { capabilityState }
        )

        voiceService = VoiceAssistantService(
            context = this,
            onStatus = { status -> runOnUiThread { appendOutput(status) } },
            onHotwordDetected = { runOnUiThread { appendOutput("Hotword detected: jarvis") } },
            onCommandDetected = { recognition ->
                runOnUiThread { handleVoiceRecognition(recognition) }
            }
        )

        bindUiListeners()
        reloadVoiceConfiguration(showStatus = true)
        refreshPrivilegeCenter()
        refreshSpecialPermissionsStatus()
        refreshShizukuRuntimeStatus()
        updateMicrophonePermissionUi()
        promptForMicrophonePermissionOnFirstLaunch()

        appendOutput("Startup capability status:\n${systemService.buildStatusSummary(capabilityState)}")
        appendOutput(getString(R.string.voice_hint))
        appendRecentAudit()
        refreshAuditDebugSection()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabMenu)
        tabSections = listOf(
            findViewById(R.id.tabAssistantSection),
            findViewById(R.id.tabProjectSection),
            findViewById(R.id.tabShizukuSection),
            findViewById(R.id.tabStatusSection),
            findViewById(R.id.tabTerminalSection),
            findViewById(R.id.tabAuditSection),
            findViewById(R.id.tabVoiceSection),
            findViewById(R.id.tabPermissionsSection),
            findViewById(R.id.tabDhizukuSection)
        )

        commandInput = findViewById(R.id.etCommandInput)
        shizukuCommandInput = findViewById(R.id.etShizukuCommand)
        terminalCommandInput = findViewById(R.id.etTerminalCommand)
        shizukuRuntimeStatusView = findViewById(R.id.tvShizukuRuntimeStatus)
        outputLog = findViewById(R.id.tvOutputLog)
        statusOutput = findViewById(R.id.tvStatusOutput)
        specialPermissionsStatus = findViewById(R.id.tvSpecialPermissionsStatus)
        terminalOutputView = findViewById(R.id.tvTerminalOutput)
        terminalHistoryView = findViewById(R.id.tvTerminalHistory)
        auditDebugOutput = findViewById(R.id.tvAuditDebugOutput)
        micPermissionStatusView = findViewById(R.id.tvMicPermissionStatus)
        grantMicPermissionButton = findViewById(R.id.btnGrantMicPermission)
        privilegeCenterView = findViewById(R.id.tvPrivilegeCenter)
        refreshPrivilegesButton = findViewById(R.id.btnRefreshPrivileges)
        requestDhizukuPermissionButton = findViewById(R.id.btnRequestDhizukuPermission)
        applyCoreDelegatedScopesButton = findViewById(R.id.btnApplyCoreDelegatedScopes)
        terminalRunButton = findViewById(R.id.btnTerminalRunCommand)
        terminalStopButton = findViewById(R.id.btnTerminalStopCommand)
        terminalClearButton = findViewById(R.id.btnTerminalClearOutput)
        refreshShizukuStatusButton = findViewById(R.id.btnRefreshShizukuStatus)
        openShizukuAppButton = findViewById(R.id.btnOpenShizukuApp)
        requestShizukuPermissionButton = findViewById(R.id.btnRequestShizukuPermission)
        auditRefreshButton = findViewById(R.id.btnAuditRefresh)
        auditClearViewButton = findViewById(R.id.btnAuditClearView)
        auditCopyButton = findViewById(R.id.btnAuditCopy)
        auditLimitInput = findViewById(R.id.etAuditLimit)
        auditStatusFilterInput = findViewById(R.id.etAuditStatusFilter)
        auditAdapterFilterInput = findViewById(R.id.etAuditAdapterFilter)

        voiceButton = findViewById(R.id.btnToggleVoice)
        settingsButton = findViewById(R.id.btnVoiceSettings)

        switchCameraDisabled = findViewById(R.id.switchCameraDisabled)
        switchScreenCaptureDisabled = findViewById(R.id.switchScreenCaptureDisabled)
        switchInstallAppsRestricted = findViewById(R.id.switchInstallAppsRestricted)
        switchUninstallAppsRestricted = findViewById(R.id.switchUninstallAppsRestricted)
        switchStatusBarDisabled = findViewById(R.id.switchStatusBarDisabled)
        switchPackageUninstallBlocked = findViewById(R.id.switchPackageUninstallBlocked)
        lockScreenMessageInput = findViewById(R.id.etLockScreenMessage)
        policyPackageNameInput = findViewById(R.id.etPolicyPackageName)

        btnApplyCameraDisabled = findViewById(R.id.btnApplyCameraDisabled)
        btnApplyScreenCaptureDisabled = findViewById(R.id.btnApplyScreenCaptureDisabled)
        btnApplyInstallAppsRestriction = findViewById(R.id.btnApplyInstallAppsRestriction)
        btnApplyUninstallAppsRestriction = findViewById(R.id.btnApplyUninstallAppsRestriction)
        btnApplyStatusBarDisabled = findViewById(R.id.btnApplyStatusBarDisabled)
        btnApplyLockScreenMessage = findViewById(R.id.btnApplyLockScreenMessage)
        btnApplyPackagePolicy = findViewById(R.id.btnApplyPackagePolicy)
        btnLockNow = findViewById(R.id.btnLockNow)
        btnRebootFromDhizuku = findViewById(R.id.btnRebootFromDhizuku)
    }

    private fun setupTabs() {
        val titles = listOf(
            getString(R.string.tab_assistant),
            getString(R.string.tab_project),
            getString(R.string.tab_shizuku),
            getString(R.string.tab_status),
            getString(R.string.tab_terminal),
            getString(R.string.tab_audit),
            getString(R.string.tab_voice),
            getString(R.string.tab_permissions),
            getString(R.string.tab_dhizuku)
        )
        titles.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }
        showTab(0)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = showTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun showTab(index: Int) {
        tabSections.forEachIndexed { i, view ->
            view.visibility = if (i == index) View.VISIBLE else View.GONE
        }
    }

    private fun bindUiListeners() {
        findViewById<Button>(R.id.btnRunAssistantCommand).setOnClickListener {
            runAssistantCommandFromInput()
        }

        findViewById<Button>(R.id.btnAssistantQuickStatus).setOnClickListener {
            val reply = hybridAssistantService.handleUserInput("show status")
            appendOutput("J.A.R.V.I.S.: ${reply.text}")
            statusOutput.text = reply.actionResult?.output ?: reply.text
        }

        findViewById<Button>(R.id.btnCreateProject).setOnClickListener {
            val defaultName = if (::currentVoiceConfig.isInitialized) currentVoiceConfig.defaultProjectName else "assistant_demo"
            val result = assistantEngine.executeAction(actionRegistry.createProjectRequest(defaultName))
            appendActionResult(result)
            appendRecentAudit()
        }

        findViewById<Button>(R.id.btnRunShizuku).setOnClickListener {
            if (!ensureShizukuReadyForExecution(::appendOutput)) {
                return@setOnClickListener
            }
            val command = shizukuCommandInput.text?.toString()?.trim().orEmpty().ifBlank { "id" }
            val result = assistantEngine.executeAction(actionRegistry.runShellRequest(command = command, confirmed = true))
            appendActionResult(result)
            appendRecentAudit()
        }

        refreshShizukuStatusButton.setOnClickListener {
            refreshShizukuRuntimeStatus()
            appendOutput("Shizuku runtime status refreshed.")
        }

        openShizukuAppButton.setOnClickListener {
            openShizukuApp()
        }

        requestShizukuPermissionButton.setOnClickListener {
            requestShizukuPermission()
        }

        terminalRunButton.setOnClickListener { runTerminalCommand() }
        terminalStopButton.setOnClickListener { stopTerminalCommand() }
        terminalClearButton.setOnClickListener {
            terminalOutputLines.clear()
            terminalOutputView.text = ""
            appendOutput("Terminal output cleared.")
        }
        auditRefreshButton.setOnClickListener {
            refreshAuditDebugSection()
            appendOutput("Audit debug logs refreshed.")
        }
        auditClearViewButton.setOnClickListener {
            auditDebugOutput.text = ""
            appendOutput("Audit debug view cleared.")
        }
        auditCopyButton.setOnClickListener {
            copyAuditLogsToClipboard()
        }

        findViewById<Button>(R.id.btnShowStatus).setOnClickListener {
            val commandText = commandInput.text?.toString()?.trim().orEmpty()
            if (commandText.isBlank()) {
                val reply = hybridAssistantService.handleUserInput("show status")
                appendOutput("J.A.R.V.I.S.: ${reply.text}")
                statusOutput.text = reply.actionResult?.output ?: reply.text
            } else {
                runAssistantCommandFromInput()
            }
        }

        voiceButton.setOnClickListener {
            if (voiceEnabled) stopVoiceHotwordMode() else ensureMicPermissionAndStartVoice()
        }

        settingsButton.setOnClickListener { openVoiceSettings() }

        grantMicPermissionButton.setOnClickListener {
            shouldStartVoiceAfterPermission = false
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        refreshPrivilegesButton.setOnClickListener {
            refreshPrivilegeCenter()
            appendOutput(getString(R.string.privilege_status_refreshed))
        }

        requestDhizukuPermissionButton.setOnClickListener { requestDhizukuPermission() }
        applyCoreDelegatedScopesButton.setOnClickListener { applyCoreDelegatedScopes() }

        findViewById<Button>(R.id.btnRequestAllRuntimePermissions).setOnClickListener {
            requestAllRuntimePermissions()
        }
        findViewById<Button>(R.id.btnRequestAllSpecialPermissions).setOnClickListener {
            requestAllSpecialPermissions()
        }
        findViewById<Button>(R.id.btnOpenOverlayPermission).setOnClickListener { openOverlayPermission() }
        findViewById<Button>(R.id.btnOpenWriteSettingsPermission).setOnClickListener { openWriteSettingsPermission() }
        findViewById<Button>(R.id.btnOpenAllFilesPermission).setOnClickListener { openAllFilesPermission() }
        findViewById<Button>(R.id.btnOpenUsageAccessPermission).setOnClickListener { openUsageAccessPermission() }
        findViewById<Button>(R.id.btnOpenBatteryOptimizationPermission).setOnClickListener { openBatteryOptimizationPermission() }
        findViewById<Button>(R.id.btnOpenNotificationPolicyPermission).setOnClickListener { openNotificationPolicyPermission() }
        findViewById<Button>(R.id.btnOpenAccessibilitySettings).setOnClickListener { openAccessibilitySettings() }

        btnApplyCameraDisabled.setOnClickListener { applyCameraDisabled() }
        btnApplyScreenCaptureDisabled.setOnClickListener { applyScreenCaptureDisabled() }
        btnApplyInstallAppsRestriction.setOnClickListener { applyInstallAppsRestriction() }
        btnApplyUninstallAppsRestriction.setOnClickListener { applyUninstallAppsRestriction() }
        btnApplyStatusBarDisabled.setOnClickListener { applyStatusBarDisabled() }
        btnApplyLockScreenMessage.setOnClickListener { applyLockScreenMessage() }
        btnApplyPackagePolicy.setOnClickListener { applyPackagePolicy() }
        btnLockNow.setOnClickListener { lockNow() }
        btnRebootFromDhizuku.setOnClickListener { rebootDevice() }
    }

    private fun runAssistantCommandFromInput() {
        val commandText = commandInput.text?.toString()?.trim().orEmpty()
        val reply = hybridAssistantService.handleUserInput(commandText)
        appendOutput("You: ${if (commandText.isBlank()) "(status request)" else commandText}")
        appendOutput("J.A.R.V.I.S.: ${reply.text}")
        statusOutput.text = reply.actionResult?.output ?: reply.text
        appendRecentAudit()
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

    private fun refreshAuditDebugSection() {
        val limit = auditLimitInput.text?.toString()?.toIntOrNull()?.coerceIn(1, 500) ?: 100
        val statusFilter = auditStatusFilterInput.text?.toString()?.trim().orEmpty()
        val adapterFilter = auditAdapterFilterInput.text?.toString()?.trim().orEmpty()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val entries = auditService.getRecentEntries(limit)
            .filter {
                statusFilter.isBlank() || (it.status ?: "").contains(statusFilter, ignoreCase = true)
            }
            .filter {
                adapterFilter.isBlank() || (it.adapterUsed ?: "").contains(adapterFilter, ignoreCase = true)
            }

        auditDebugOutput.text = if (entries.isEmpty()) {
            "No audit entries match current filters."
        } else {
            entries.joinToString(separator = "\n\n") { entry ->
                buildString {
                    appendLine("id: ${entry.id}")
                    appendLine("actionId: ${entry.actionId ?: "N/A"}")
                    appendLine("time: ${formatter.format(Date(entry.createdAt))}")
                    appendLine("status: ${entry.status ?: "UNKNOWN"}")
                    appendLine("adapter: ${entry.adapterUsed ?: "N/A"}")
                    append("message: ${entry.message ?: ""}")
                }
            }
        }
    }

    private fun copyAuditLogsToClipboard() {
        val text = auditDebugOutput.text?.toString().orEmpty()
        if (text.isBlank()) {
            appendOutput("Audit debug output is empty; nothing copied.")
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("audit_logs", text))
        appendOutput("Audit debug logs copied to clipboard.")
    }

    private fun appendOutput(text: String) {
        outputLines.add(text)
        outputLog.text = outputLines.joinToString(separator = "\n\n")
    }

    private fun appendTerminalOutput(line: String) {
        terminalOutputLines.add(line)
        terminalOutputView.text = terminalOutputLines.joinToString(separator = "\n")
    }

    private fun runTerminalCommand() {
        if (runningTerminalCommand != null) {
            appendTerminalOutput("[!] A command is already running. Stop it first.")
            return
        }
        if (!ensureShizukuReadyForExecution(::appendTerminalOutput)) {
            return
        }

        val rawCommand = terminalCommandInput.text?.toString()?.trim().orEmpty().ifBlank { "id" }
        val parsed = shizukuShellService.parseRishCommand(rawCommand)
        if (parsed.interactiveOnly) {
            val helper = shizukuShellService.ensureRishHelperFile().absolutePath
            appendTerminalOutput("[!] Interactive .rish shell is not supported in this UI.")
            appendTerminalOutput("[i] Use: .rish -c \"<command>\"")
            appendTerminalOutput("[i] Helper file: $helper")
            return
        }

        terminalHistory.add(0, rawCommand)
        if (terminalHistory.size > 50) {
            terminalHistory.removeAt(terminalHistory.lastIndex)
        }
        refreshTerminalHistory()
        appendTerminalOutput("$ $rawCommand")

        try {
            runningTerminalCommand = shizukuShellService.runStreaming(
                parsed = parsed,
                onStdout = { line -> runOnUiThread { appendTerminalOutput(line) } },
                onStderr = { line -> runOnUiThread { appendTerminalOutput("[err] $line") } },
                onCompleted = { exitCode, timedOut ->
                    runOnUiThread {
                        appendTerminalOutput("[exit=$exitCode timedOut=$timedOut]")
                        runningTerminalCommand = null
                    }
                },
                onError = { message ->
                    runOnUiThread {
                        appendTerminalOutput("[error] $message")
                        appendTerminalOutput("[i] ${buildShizukuStateSummary()}")
                        runningTerminalCommand = null
                    }
                }
            )
        } catch (error: Throwable) {
            appendTerminalOutput("[error] ${error.message ?: "Unable to start Shizuku command."}")
            appendTerminalOutput("[i] ${buildShizukuStateSummary()}")
        }
    }

    private fun stopTerminalCommand() {
        val running = runningTerminalCommand
        if (running == null) {
            appendTerminalOutput("[i] No running command.")
            return
        }
        running.stop()
        runningTerminalCommand = null
        appendTerminalOutput("[i] Command stopped.")
    }

    private fun refreshTerminalHistory() {
        terminalHistoryView.text = terminalHistory.joinToString(separator = "\n")
    }

    private fun refreshShizukuRuntimeStatus() {
        val binderReady = isShizukuBinderReady()
        val permissionState = getShizukuPermissionState()
        val permissionLabel = when (permissionState) {
            PackageManager.PERMISSION_GRANTED -> "granted"
            PackageManager.PERMISSION_DENIED -> "denied"
            null -> "unknown (binder not received)"
            else -> "unknown ($permissionState)"
        }
        shizukuRuntimeStatusView.text = buildString {
            appendLine("binderReady: $binderReady")
            appendLine("permission: $permissionLabel")
            append("capabilityFlag: ${if (::capabilityState.isInitialized) capabilityState.shizuku else false}")
        }
        requestShizukuPermissionButton.isEnabled = binderReady && permissionState != PackageManager.PERMISSION_GRANTED
        if (::capabilityState.isInitialized) {
            capabilityState = capabilityState.copy(shizuku = binderReady)
        }
    }

    private fun ensureShizukuReadyForExecution(onMessage: (String) -> Unit): Boolean {
        if (!isShizukuBinderReady()) {
            onMessage("Shizuku binder not connected. Open Shizuku app and start the service first.")
            onMessage("Tap 'Open Shizuku App', start service, then tap 'Refresh Shizuku Status'.")
            refreshShizukuRuntimeStatus()
            return false
        }
        val permissionState = getShizukuPermissionState()
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            onMessage("Shizuku permission is not granted. Tap 'Request Shizuku Permission'.")
            refreshShizukuRuntimeStatus()
            return false
        }
        return true
    }

    private fun openShizukuApp() {
        val candidates = listOf("moe.shizuku.privileged.api", "rikka.shizuku")
        val intent = candidates
            .asSequence()
            .mapNotNull { packageManager.getLaunchIntentForPackage(it) }
            .firstOrNull()
        if (intent == null) {
            appendOutput("Shizuku app is not installed on this device.")
            return
        }
        startActivity(intent)
    }

    private fun requestShizukuPermission() {
        if (!isShizukuBinderReady()) {
            appendOutput("Cannot request permission: Shizuku binder not connected. Start Shizuku service first.")
            refreshShizukuRuntimeStatus()
            return
        }
        try {
            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            appendOutput("Requested Shizuku permission.")
        } catch (error: Throwable) {
            appendOutput("Failed to request Shizuku permission: ${error.message ?: "unknown error"}")
        }
        refreshShizukuRuntimeStatus()
    }

    private fun isShizukuBinderReady(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    private fun getShizukuPermissionState(): Int? {
        if (!isShizukuBinderReady()) return null
        return try {
            Shizuku.checkSelfPermission()
        } catch (_: Throwable) {
            null
        }
    }

    private fun buildShizukuStateSummary(): String {
        val binderReady = isShizukuBinderReady()
        val permissionState = when (getShizukuPermissionState()) {
            PackageManager.PERMISSION_GRANTED -> "granted"
            PackageManager.PERMISSION_DENIED -> "denied"
            null -> "unknown (binder haven't been received)"
            else -> "unknown"
        }
        return "Shizuku state -> binderReady=$binderReady, permission=$permissionState."
    }

    private fun handleVoiceRecognition(recognition: VoiceRecognitionResult) {
        val command = recognition.transcript
        commandInput.setText(command)
        appendOutput("Voice command (${(recognition.confidence * 100f).toInt()}%): $command")

        pendingClarification?.let { awaiting ->
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
                val directResult = parsed.actionRequest?.let { assistantEngine.executeAction(it) }
                if (directResult != null) {
                    appendActionResult(directResult)
                    statusOutput.text = directResult.output ?: directResult.message
                    voiceService.speak(if (directResult.success) directResult.message else "I couldn't complete that command.")
                } else {
                    val reply = hybridAssistantService.handleUserInput(parsed.fallbackTextCommand ?: command)
                    appendOutput("J.A.R.V.I.S.: ${reply.text}")
                    statusOutput.text = reply.actionResult?.output ?: reply.text
                    voiceService.speak(reply.text.lineSequence().firstOrNull()?.take(180) ?: "Done.")
                }
                appendRecentAudit()
            }
        }
    }

    private fun ensureMicPermissionAndStartVoice() {
        if (hasMicrophonePermission()) {
            startVoiceHotwordMode()
        } else {
            shouldStartVoiceAfterPermission = true
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceHotwordMode() {
        shouldStartVoiceAfterPermission = false
        pendingClarification = null
        if (currentVoiceConfig.enableDedicatedWakeWord && currentVoiceConfig.porcupineAccessKey.isBlank()) {
            appendOutput("Porcupine AccessKey is not configured; using speech fallback mode.")
        }
        if (currentVoiceConfig.useForegroundServiceMode) {
            VoiceForegroundService.start(this)
            voiceEnabled = true
            appendOutput(getString(R.string.voice_service_started))
        } else {
            voiceEnabled = true
            appendOutput(getString(R.string.voice_started))
            voiceService.startHotwordLoop()
            appendOutput(getString(R.string.voice_engine_label, voiceService.getCurrentHotwordEngine().name))
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

    private fun openVoiceSettings() {
        voiceSettingsLauncher.launch(Intent(this, VoiceSettingsActivity::class.java))
    }

    private fun reloadVoiceConfiguration(showStatus: Boolean) {
        currentVoiceConfig = voicePreferences.load()
        voiceService.updateConfig(currentVoiceConfig)
        capabilityState = CapabilityDetector().detect(this)
        if (currentVoiceConfig.useForegroundServiceMode && voiceEnabled) {
            voiceService.stopListening()
        }
        voiceEnabled = if (currentVoiceConfig.useForegroundServiceMode) {
            voicePreferences.isForegroundServiceRunning()
        } else {
            voiceEnabled
        }
        if (showStatus) {
            appendOutput(getString(R.string.voice_engine_label, voiceService.getCurrentHotwordEngine().name))
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
        refreshPrivilegeCenter()
    }

    private fun refreshVoiceButtonLabel() {
        if (!::currentVoiceConfig.isInitialized) return
        voiceButton.text = if (currentVoiceConfig.useForegroundServiceMode) {
            if (voiceEnabled) getString(R.string.stop_voice_service) else getString(R.string.start_voice_service)
        } else {
            if (voiceEnabled) getString(R.string.stop_voice_hotword) else getString(R.string.start_voice_hotword)
        }
    }

    private fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun updateMicrophonePermissionUi() {
        val granted = hasMicrophonePermission()
        micPermissionStatusView.text = if (granted) {
            getString(R.string.mic_permission_status_ok)
        } else {
            getString(R.string.mic_permission_status_missing)
        }
        grantMicPermissionButton.isEnabled = !granted
    }

    private fun promptForMicrophonePermissionOnFirstLaunch() {
        val alreadyPrompted = uiPreferences.getBoolean(KEY_PROMPTED_MIC_PERMISSION, false)
        if (!alreadyPrompted && !hasMicrophonePermission()) {
            uiPreferences.edit().putBoolean(KEY_PROMPTED_MIC_PERMISSION, true).apply()
            appendOutput(getString(R.string.mic_permission_prompt_startup))
            shouldStartVoiceAfterPermission = false
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun requestDhizukuPermission() {
        dhizukuService.requestPermission(this) { granted, message ->
            runOnUiThread {
                appendOutput(message)
                refreshPrivilegeCenter()
                if (granted) appendOutput(getString(R.string.dhizuku_permission_granted_hint))
            }
        }
    }

    private fun applyCoreDelegatedScopes() {
        dhizukuService.applyCoreDelegatedScopes(this) { success, message ->
            runOnUiThread {
                appendOutput(message)
                if (success) refreshPrivilegeCenter()
            }
        }
    }

    private fun refreshPrivilegeCenter() {
        currentDhizukuStatus = dhizukuService.getStatus(this)
        capabilityState = capabilityState.copy(dhizuku = currentDhizukuStatus.initialized)
        privilegeCenterView.text = privilegeCatalogService.buildPrivilegeOverview(
            capabilityState = capabilityState,
            dhizukuStatus = currentDhizukuStatus
        )
        requestDhizukuPermissionButton.isEnabled = currentDhizukuStatus.initialized && !currentDhizukuStatus.permissionGranted
        applyCoreDelegatedScopesButton.isEnabled = currentDhizukuStatus.initialized && currentDhizukuStatus.permissionGranted
        refreshDeviceOwnerControlsAvailability()
    }

    private fun refreshDeviceOwnerControlsAvailability() {
        val enabled = resolvePolicyComponent() != null
        val controls = listOf(
            switchCameraDisabled,
            switchScreenCaptureDisabled,
            switchInstallAppsRestricted,
            switchUninstallAppsRestricted,
            switchStatusBarDisabled,
            switchPackageUninstallBlocked,
            lockScreenMessageInput,
            policyPackageNameInput,
            btnApplyCameraDisabled,
            btnApplyScreenCaptureDisabled,
            btnApplyInstallAppsRestriction,
            btnApplyUninstallAppsRestriction,
            btnApplyStatusBarDisabled,
            btnApplyLockScreenMessage,
            btnApplyPackagePolicy,
            btnLockNow,
            btnRebootFromDhizuku
        )
        controls.forEach { it.isEnabled = enabled }
    }

    private fun resolvePolicyComponent(): ComponentName? {
        return try {
            when {
                currentDhizukuStatus.initialized && currentDhizukuStatus.permissionGranted -> {
                    Dhizuku.getOwnerComponent(this)
                }
                capabilityState.deviceOwner -> {
                    Dhizuku.getOwnerComponent(devicePolicyManager)
                }
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun applyCameraDisabled() = withPolicyComponent { admin ->
        devicePolicyManager.setCameraDisabled(admin, switchCameraDisabled.isChecked)
        appendOutput("Camera disabled set to ${switchCameraDisabled.isChecked}")
    }

    private fun applyScreenCaptureDisabled() = withPolicyComponent { admin ->
        devicePolicyManager.setScreenCaptureDisabled(admin, switchScreenCaptureDisabled.isChecked)
        appendOutput("Screen capture disabled set to ${switchScreenCaptureDisabled.isChecked}")
    }

    private fun applyInstallAppsRestriction() = withPolicyComponent { admin ->
        applyUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS, switchInstallAppsRestricted.isChecked)
        appendOutput("Install apps restriction set to ${switchInstallAppsRestricted.isChecked}")
    }

    private fun applyUninstallAppsRestriction() = withPolicyComponent { admin ->
        applyUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS, switchUninstallAppsRestricted.isChecked)
        appendOutput("Uninstall apps restriction set to ${switchUninstallAppsRestricted.isChecked}")
    }

    private fun applyStatusBarDisabled() = withPolicyComponent { admin ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val applied = devicePolicyManager.setStatusBarDisabled(admin, switchStatusBarDisabled.isChecked)
            appendOutput("Status bar disabled set to ${switchStatusBarDisabled.isChecked} (applied=$applied)")
        } else {
            appendOutput("Status bar policy requires Android 6+.")
        }
    }

    private fun applyLockScreenMessage() = withPolicyComponent { admin ->
        val message = lockScreenMessageInput.text?.toString().orEmpty()
        devicePolicyManager.setDeviceOwnerLockScreenInfo(admin, message)
        appendOutput("Lock screen message updated.")
    }

    private fun applyPackagePolicy() = withPolicyComponent { admin ->
        val packageName = policyPackageNameInput.text?.toString()?.trim().orEmpty()
        if (packageName.isBlank()) {
            appendOutput("Package name is required.")
            return@withPolicyComponent
        }
        devicePolicyManager.setUninstallBlocked(admin, packageName, switchPackageUninstallBlocked.isChecked)
        appendOutput("Package uninstall policy set for $packageName = ${switchPackageUninstallBlocked.isChecked}")
    }

    private fun lockNow() = withPolicyComponent {
        devicePolicyManager.lockNow()
        appendOutput("Lock now command sent.")
    }

    private fun rebootDevice() = withPolicyComponent { admin ->
        devicePolicyManager.reboot(admin)
        appendOutput("Reboot command sent.")
    }

    private fun applyUserRestriction(admin: ComponentName, key: String, enabled: Boolean) {
        if (enabled) {
            devicePolicyManager.addUserRestriction(admin, key)
        } else {
            devicePolicyManager.clearUserRestriction(admin, key)
        }
    }

    private fun withPolicyComponent(action: (ComponentName) -> Unit) {
        val admin = resolvePolicyComponent()
        if (admin == null) {
            appendOutput("No active Dhizuku/device-owner component. Request Dhizuku permission first.")
            return
        }
        try {
            action(admin)
            refreshPrivilegeCenter()
        } catch (error: SecurityException) {
            appendOutput("Policy action blocked: ${error.message ?: "security exception"}")
        } catch (error: Throwable) {
            appendOutput("Policy action failed: ${error.message ?: "unknown error"}")
        }
    }

    private fun requestAllRuntimePermissions() {
        runtimePermissionsLauncher.launch(buildRuntimePermissionList().toTypedArray())
    }

    private fun buildRuntimePermissionList(): List<String> {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions += Manifest.permission.BLUETOOTH_CONNECT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
            permissions += Manifest.permission.READ_MEDIA_IMAGES
            permissions += Manifest.permission.READ_MEDIA_VIDEO
            permissions += Manifest.permission.READ_MEDIA_AUDIO
        } else {
            permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return permissions.distinct()
    }

    private fun requestAllSpecialPermissions() {
        val opened = openNextMissingSpecialPermission()
        if (!opened) {
            appendOutput("All tracked special permissions already appear granted.")
        }
        refreshSpecialPermissionsStatus()
    }

    private fun openNextMissingSpecialPermission(): Boolean {
        return when {
            !Settings.canDrawOverlays(this) -> {
                openOverlayPermission(); true
            }
            !Settings.System.canWrite(this) -> {
                openWriteSettingsPermission(); true
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !android.os.Environment.isExternalStorageManager() -> {
                openAllFilesPermission(); true
            }
            !hasUsageAccess() -> {
                openUsageAccessPermission(); true
            }
            !isIgnoringBatteryOptimizations() -> {
                openBatteryOptimizationPermission(); true
            }
            !isNotificationPolicyAccessGranted() -> {
                openNotificationPolicyPermission(); true
            }
            !isAccessibilityEnabledForApp() -> {
                openAccessibilitySettings(); true
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms() -> {
                openExactAlarmPermission(); true
            }
            else -> false
        }
    }

    private fun refreshSpecialPermissionsStatus() {
        specialPermissionsStatus.text = buildString {
            appendLine("Runtime permissions:")
            buildRuntimePermissionList().forEach { permission ->
                val granted = ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED
                appendLine("- $permission: $granted")
            }
            appendLine()
            appendLine("Special app-access permissions:")
            appendLine("- Overlay: ${Settings.canDrawOverlays(this@MainActivity)}")
            appendLine("- Write Settings: ${Settings.System.canWrite(this@MainActivity)}")
            appendLine("- All Files Access: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) android.os.Environment.isExternalStorageManager() else "N/A"}")
            appendLine("- Usage Access: ${hasUsageAccess()}")
            appendLine("- Battery Optimization Exemption: ${isIgnoringBatteryOptimizations()}")
            appendLine("- Notification Policy Access: ${isNotificationPolicyAccessGranted()}")
            appendLine("- Accessibility Enabled For App: ${isAccessibilityEnabledForApp()}")
            appendLine("- Schedule Exact Alarms: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) canScheduleExactAlarms() else "N/A"}")
        }
    }

    private fun openOverlayPermission() {
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
    }

    private fun openWriteSettingsPermission() {
        startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
    }

    private fun openAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName")))
        }
    }

    private fun openUsageAccessPermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun openBatteryOptimizationPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
        startActivity(intent)
    }

    private fun openNotificationPolicyPermission() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun openExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
        }
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun isNotificationPolicyAccessGranted(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    private fun isAccessibilityEnabledForApp(): Boolean {
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(packageName, ignoreCase = true) == true
    }

    private fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
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

    private fun registerShizukuListeners() {
        if (shizukuListenersRegistered) return
        try {
            Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceivedListener)
            Shizuku.addBinderDeadListener(shizukuBinderDeadListener)
            Shizuku.addRequestPermissionResultListener(shizukuPermissionResultListener)
            shizukuListenersRegistered = true
        } catch (_: Throwable) {
            // ignore; status UI still works through manual refresh checks
        }
    }

    private fun unregisterVoiceEventReceiver() {
        if (!receiverRegistered) return
        unregisterReceiver(voiceEventReceiver)
        receiverRegistered = false
    }

    private fun unregisterShizukuListeners() {
        if (!shizukuListenersRegistered) return
        try {
            Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener)
            Shizuku.removeBinderDeadListener(shizukuBinderDeadListener)
            Shizuku.removeRequestPermissionResultListener(shizukuPermissionResultListener)
        } catch (_: Throwable) {
            // ignore
        }
        shizukuListenersRegistered = false
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

    override fun onStart() {
        super.onStart()
        registerVoiceEventReceiver()
        registerShizukuListeners()
    }

    override fun onStop() {
        unregisterShizukuListeners()
        unregisterVoiceEventReceiver()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        reloadVoiceConfiguration(showStatus = false)
        updateMicrophonePermissionUi()
        refreshSpecialPermissionsStatus()
        refreshShizukuRuntimeStatus()
        refreshAuditDebugSection()
    }

    override fun onDestroy() {
        runningTerminalCommand?.stop()
        runningTerminalCommand = null
        voiceService.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val KEY_PROMPTED_MIC_PERMISSION = "prompted_mic_permission"
        private const val SHIZUKU_PERMISSION_REQUEST_CODE = 9567
    }
}
