package com.assistant.core

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.assistant.core.engine.AssistantEngine
import com.assistant.core.models.ActionRequest
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var engine: AssistantEngine
    private lateinit var etCommand: EditText
    private lateinit var tvOutput: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        engine = AssistantEngine(this)
        engine.initialize()

        etCommand = findViewById(R.id.etCommand)
        tvOutput = findViewById(R.id.tvOutput)

        findViewById<Button>(R.id.btnCreateProject).setOnClickListener {
            val request = ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "CREATE_PROJECT",
                parameters = mapOf("name" to "assistant_demo"),
                requiredCapability = "STANDARD",
                riskLevel = 1
            )
            val result = engine.executeRequest(request)
            appendOutput(buildString {
                appendLine("[CREATE_PROJECT]")
                appendLine("Success: ${result.success}")
                appendLine("Adapter: ${result.adapterUsed}")
                appendLine("Message: ${result.message}")
                result.output?.let { appendLine("Output: $it") }
            })
        }

        findViewById<Button>(R.id.btnShizukuTest).setOnClickListener {
            val request = ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "RUN_SHELL",
                parameters = mapOf("command" to "id"),
                requiredCapability = "SHIZUKU",
                riskLevel = 2
            )
            val result = engine.executeRequest(request)
            appendOutput(buildString {
                appendLine("[SHIZUKU_TEST]")
                appendLine("Success: ${result.success}")
                appendLine("Adapter: ${result.adapterUsed}")
                appendLine("Message: ${result.message}")
                result.output?.let { appendLine("Output: $it") }
            })
        }

        findViewById<Button>(R.id.btnCapabilityStatus).setOnClickListener {
            val request = ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "SHOW_STATUS",
                parameters = emptyMap(),
                requiredCapability = "STANDARD",
                riskLevel = 0
            )
            val result = engine.executeRequest(request)
            appendOutput(buildString {
                appendLine("[CAPABILITY_STATUS]")
                result.output?.let { appendLine(it) } ?: appendLine(result.message)
            })
        }

        findViewById<Button>(R.id.btnSubmitCommand).setOnClickListener {
            val input = etCommand.text.toString().trim()
            if (input.isNotEmpty()) {
                val result = engine.processCommand(input)
                appendOutput(buildString {
                    appendLine("[COMMAND: $input]")
                    appendLine("Success: ${result.success}")
                    appendLine("Adapter: ${result.adapterUsed}")
                    appendLine("Message: ${result.message}")
                    result.output?.let { appendLine("Output: $it") }
                })
                etCommand.setText("")
            }
        }

        // Show initial capability status on startup
        val caps = engine.currentCapabilityState()
        appendOutput(
            "Assistant Platform initialized.\n" +
            "Capabilities: standard=${caps.standard}, shizuku=${caps.shizuku}, " +
            "deviceOwner=${caps.deviceOwner}, dhizuku=${caps.dhizuku}\n"
        )
    }

    private fun appendOutput(text: String) {
        val current = tvOutput.text.toString()
        tvOutput.text = text + "\n" + current
    }
}
