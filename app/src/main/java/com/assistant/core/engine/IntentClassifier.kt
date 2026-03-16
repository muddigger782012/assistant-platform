package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import java.util.Locale

class IntentClassifier(private val actionRegistry: ActionRegistry) {

    fun classify(rawInput: String): ActionRequest? {
        val normalized = rawInput.trim().lowercase(Locale.US)
        if (normalized.isBlank()) {
            return null
        }
        return when {
            normalized.contains("create project") -> {
                val projectName = normalized.substringAfter("create project", "").trim().ifEmpty { "assistant_demo" }
                actionRegistry.createRequest(
                    ActionRegistry.ACTION_CREATE_PROJECT,
                    parameters = mapOf("name" to projectName),
                )
            }

            normalized.contains("run shell") -> {
                val command = normalized.substringAfter("run shell", "").trim().ifEmpty { "id" }
                actionRegistry.createRequest(
                    ActionRegistry.ACTION_RUN_SHELL,
                    parameters = mapOf("command" to command),
                )
            }

            normalized.contains("reboot") -> actionRegistry.createRequest(ActionRegistry.ACTION_REBOOT_DEVICE)
            normalized.contains("show status") || normalized.contains("capability") ->
                actionRegistry.createRequest(ActionRegistry.ACTION_SHOW_STATUS)

            else -> null
        }
    }
}
