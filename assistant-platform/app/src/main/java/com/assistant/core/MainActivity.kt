package com.assistant.core

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.assistant.core.engine.AssistantEngine
import com.assistant.core.models.ActionRequest
import com.assistant.core.services.AuditService
import com.assistant.core.storage.ActionRepository
import com.assistant.core.storage.AuditRepository
import com.assistant.core.storage.Database
import com.assistant.core.storage.ProjectRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var database: Database
    private lateinit var assistantEngine: AssistantEngine
    private lateinit var auditService: AuditService
    private lateinit var projectRepository: ProjectRepository

    private lateinit var editCommandInput: EditText
    private lateinit var btnCreateProject: Button
    private lateinit var btnRunShizuku: Button
    private lateinit var btnShowStatus: Button
    private lateinit var tvOutputLog: TextView

    private val outputBuffer = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponents()
        bindViews()
        setupListeners()

        appendOutput("Assistant Platform initialized")
        appendOutput("Detecting capabilities...")
        assistantEngine.refreshCapabilities()
        appendOutput(assistantEngine.capabilityState.toString())
    }

    private fun initComponents() {
        database = Database(this)
        val auditRepository = AuditRepository(database)
        val actionRepository = ActionRepository(database)
        projectRepository = ProjectRepository(database)
        auditService = AuditService(auditRepository)
        assistantEngine = AssistantEngine(this, auditService, actionRepository)
    }

    private fun bindViews() {
        editCommandInput = findViewById(R.id.editCommandInput)
        btnCreateProject = findViewById(R.id.btnCreateProject)
        btnRunShizuku = findViewById(R.id.btnRunShizuku)
        btnShowStatus = findViewById(R.id.btnShowStatus)
        tvOutputLog = findViewById(R.id.tvOutputLog)
    }

    private fun setupListeners() {
        btnCreateProject.setOnClickListener {
            appendOutput("\n--- Create Project ---")
            val request = ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "CREATE_PROJECT",
                parameters = mapOf("name" to "assistant_demo"),
                requiredCapability = "STANDARD",
                riskLevel = 1
            )
            val result = assistantEngine.executeAction(request)
            appendOutput("Result: ${result.message}")
            result.output?.let { appendOutput(it) }

            if (result.success) {
                val project = com.assistant.core.models.Project(
                    id = UUID.randomUUID().toString(),
                    name = "assistant_demo",
                    rootPath = "${filesDir.absolutePath}/projects/assistant_demo",
                    createdAt = System.currentTimeMillis()
                )
                projectRepository.insertProject(project)
                appendOutput("Project record saved to database")
            }
        }

        btnRunShizuku.setOnClickListener {
            appendOutput("\n--- Shizuku Test ---")
            val request = ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "RUN_SHELL",
                parameters = mapOf("command" to "id"),
                requiredCapability = "SHIZUKU",
                riskLevel = 2
            )
            val result = assistantEngine.executeAction(request, confirmed = true)
            appendOutput("Result: ${result.message}")
            result.output?.let { appendOutput(it) }
        }

        btnShowStatus.setOnClickListener {
            appendOutput("\n--- Capability Status ---")
            val request = ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "SHOW_STATUS",
                parameters = emptyMap(),
                requiredCapability = "STANDARD",
                riskLevel = 0
            )
            val result = assistantEngine.executeAction(request)
            appendOutput("Result: ${result.message}")
            result.output?.let { appendOutput(it) }

            appendOutput("\n--- Recent Audit Log ---")
            val entries = auditService.getRecentEntries(10)
            if (entries.isEmpty()) {
                appendOutput("No audit entries yet.")
            } else {
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                for (entry in entries) {
                    appendOutput("[${sdf.format(Date(entry.createdAt))}] ${entry.status}: ${entry.message}")
                }
            }
        }
    }

    private fun appendOutput(text: String) {
        outputBuffer.appendLine(text)
        tvOutputLog.text = outputBuffer.toString()
    }
}
