package com.assistant.core

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import com.assistant.core.services.AuditService
import com.assistant.core.services.CodingService
import com.assistant.core.services.FileService
import com.assistant.core.services.SystemService
import com.assistant.core.storage.ActionRepository
import com.assistant.core.storage.AuditRepository
import com.assistant.core.storage.Database
import com.assistant.core.storage.ProjectRepository
import java.text.DateFormat
import java.util.Date
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var commandInput: EditText
    private lateinit var outputLogText: TextView
    private lateinit var outputScroll: ScrollView

    private lateinit var actionRegistry: ActionRegistry
    private lateinit var systemService: SystemService
    private lateinit var auditService: AuditService
    private lateinit var assistantEngine: AssistantEngine
    private lateinit var shizukuAdapter: ShizukuAdapter

    private val sessionMessages = mutableListOf<String>()

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode != SHIZUKU_PERMISSION_REQUEST_CODE) {
            return@OnRequestPermissionResultListener
        }
        val granted = grantResult == PackageManager.PERMISSION_GRANTED
        appendSessionMessage(
            if (granted) {
                "Shizuku permission granted."
            } else {
                "Shizuku permission denied."
            },
        )
        if (granted) {
            runShizukuTest()
        } else {
            assistantEngine.refreshCapabilities()
            renderOutput()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        commandInput = findViewById(R.id.commandInput)
        outputLogText = findViewById(R.id.outputLogText)
        outputScroll = findViewById(R.id.outputScroll)

        initializeEngine()
        bindUi()

        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)

        assistantEngine.refreshCapabilities()
        appendSessionMessage("Assistant Platform started.")
        renderOutput()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
        super.onDestroy()
    }

    private fun initializeEngine() {
        val database = Database(this)
        val projectRepository = ProjectRepository(database)
        val actionRepository = ActionRepository(database)
        val auditRepository = AuditRepository(database)

        val fileService = FileService(this)
        val codingService = CodingService(this, fileService)
        systemService = SystemService(this)
        auditService = AuditService(auditRepository)

        val capabilityDetector = CapabilityDetector(this)
        actionRegistry = ActionRegistry()
        val standardAdapter = StandardAdapter(
            codingService = codingService,
            fileService = fileService,
            systemService = systemService,
            projectRepository = projectRepository,
        )
        shizukuAdapter = ShizukuAdapter()
        val capabilityBroker = CapabilityBroker(
            standardAdapter = standardAdapter,
            specialAccessAdapter = SpecialAccessAdapter(),
            shizukuAdapter = shizukuAdapter,
            dhizukuAdapter = DhizukuAdapter(),
        )

        assistantEngine = AssistantEngine(
            capabilityDetector = capabilityDetector,
            intentClassifier = IntentClassifier(actionRegistry),
            policyGate = PolicyGate(),
            actionExecutor = ActionExecutor(capabilityBroker),
            actionRepository = actionRepository,
            auditService = auditService,
        )
    }

    private fun bindUi() {
        findViewById<Button>(R.id.submitCommandButton).setOnClickListener {
            val command = commandInput.text.toString().trim()
            if (command.isBlank()) {
                appendSessionMessage("Enter a command before submitting.")
                renderOutput()
            } else {
                val result = assistantEngine.executeCommand(command, explicitConfirmation = true)
                appendResult(result.message, result.output)
            }
        }

        findViewById<Button>(R.id.createProjectButton).setOnClickListener {
            val request = actionRegistry.createRequest(
                ActionRegistry.ACTION_CREATE_PROJECT,
                parameters = mapOf("name" to "assistant_demo"),
            )
            if (request == null) {
                appendSessionMessage("CREATE_PROJECT is not registered.")
                renderOutput()
            } else {
                val result = assistantEngine.executeAction(request, explicitConfirmation = true)
                appendResult(result.message, result.output)
            }
        }

        findViewById<Button>(R.id.runShizukuButton).setOnClickListener {
            val capabilityState = assistantEngine.refreshCapabilities()
            if (!capabilityState.shizuku) {
                appendSessionMessage("Shizuku is unavailable on this device.")
                renderOutput()
                return@setOnClickListener
            }

            if (!shizukuAdapter.hasPermission()) {
                appendSessionMessage("Requesting Shizuku permission.")
                renderOutput()
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
                return@setOnClickListener
            }

            runShizukuTest()
        }

        findViewById<Button>(R.id.showCapabilityStatusButton).setOnClickListener {
            val request = actionRegistry.createRequest(ActionRegistry.ACTION_SHOW_STATUS)
            if (request == null) {
                appendSessionMessage("SHOW_STATUS is not registered.")
                renderOutput()
            } else {
                val result = assistantEngine.executeAction(request, explicitConfirmation = true)
                appendResult(result.message, result.output)
            }
        }
    }

    private fun runShizukuTest() {
        val request = actionRegistry.createRequest(
            ActionRegistry.ACTION_RUN_SHELL,
            parameters = mapOf("command" to "id"),
        )
        if (request == null) {
            appendSessionMessage("RUN_SHELL is not registered.")
            renderOutput()
            return
        }

        val result = assistantEngine.executeAction(request, explicitConfirmation = true)
        appendResult(result.message, result.output)
    }

    private fun appendResult(message: String, output: String?) {
        appendSessionMessage(message)
        if (!output.isNullOrBlank()) {
            appendSessionMessage(output)
        }
        renderOutput()
    }

    private fun appendSessionMessage(message: String) {
        val timestamp = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
        sessionMessages += "[$timestamp] $message"
    }

    private fun renderOutput() {
        val capabilityState = assistantEngine.getCapabilityState()
        val capabilitySummary = systemService.buildStatusSummary(capabilityState)
        val auditEntries = auditService.getRecentLogs(limit = 10)

        val text = buildString {
            appendLine(capabilitySummary)
            appendLine()
            appendLine("Recent audit log entries:")
            if (auditEntries.isEmpty()) {
                appendLine("- none yet")
            } else {
                auditEntries.forEach { entry ->
                    appendLine("- [${entry.status ?: "unknown"}] ${entry.adapterUsed ?: "n/a"}: ${entry.message ?: ""}")
                }
            }
            appendLine()
            appendLine("Session output:")
            if (sessionMessages.isEmpty()) {
                appendLine("- no actions yet")
            } else {
                sessionMessages.forEach { appendLine(it) }
            }
        }.trim()

        outputLogText.text = text
        outputScroll.post { outputScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    companion object {
        private const val SHIZUKU_PERMISSION_REQUEST_CODE = 1001
    }
}
