package com.assistant.core

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import com.assistant.core.services.AuditService
import com.assistant.core.services.CodingService
import com.assistant.core.services.FileService
import com.assistant.core.services.SystemService
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

    private val outputLines = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        commandInput = findViewById(R.id.etCommandInput)
        outputLog = findViewById(R.id.tvOutputLog)
        val createButton: Button = findViewById(R.id.btnCreateProject)
        val shizukuButton: Button = findViewById(R.id.btnRunShizuku)
        val statusButton: Button = findViewById(R.id.btnShowStatus)

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

        appendOutput("Startup capability status:\n${systemService.buildStatusSummary(capabilityState)}")
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
}
