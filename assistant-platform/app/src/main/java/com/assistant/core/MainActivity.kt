package com.assistant.core

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.assistant.core.engine.ActionRegistry
import com.assistant.core.engine.AssistantEngine

class MainActivity : AppCompatActivity() {

    private lateinit var engine: AssistantEngine
    private lateinit var commandInput: EditText
    private lateinit var outputLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        engine = AssistantEngine(this)
        engine.detectCapabilities()

        commandInput = findViewById(R.id.commandInput)
        outputLog = findViewById(R.id.outputLog)

        appendOutput("Capability status at startup:\n${engine.capabilityState}")

        findViewById<Button>(R.id.btnCreateProject).setOnClickListener {
            val request = ActionRegistry.createProject("assistant_demo")
            val result = engine.execute(request)
            appendResult(result)
        }

        findViewById<Button>(R.id.btnRunShizuku).setOnClickListener {
            val request = ActionRegistry.runShell("id")
            val result = engine.execute(request)
            appendResult(result)
        }

        findViewById<Button>(R.id.btnShowStatus).setOnClickListener {
            val request = ActionRegistry.showStatus()
            val result = engine.execute(request)
            appendResult(result)
        }
    }

    private fun appendResult(result: com.assistant.core.models.ActionResult) {
        val text = buildString {
            append("[${if (result.success) "OK" else "FAIL"}] ")
            append(result.message)
            result.output?.let { append("\n$it") }
        }
        appendOutput(text)
    }

    private fun appendOutput(text: String) {
        val current = outputLog.text.toString()
        val newText = if (current == getString(R.string.output_placeholder)) {
            text
        } else {
            "$current\n\n$text"
        }
        outputLog.text = newText
    }
}
