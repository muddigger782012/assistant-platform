package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import java.util.UUID

class IntentClassifier {

    fun classify(input: String): ActionRequest {
        val lower = input.lowercase().trim()
        return when {
            lower.contains("create project") -> ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "CREATE_PROJECT",
                parameters = mapOf("name" to "assistant_demo"),
                requiredCapability = "STANDARD",
                riskLevel = 1
            )
            lower.contains("run shell") -> ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "RUN_SHELL",
                parameters = mapOf("command" to "id"),
                requiredCapability = "SHIZUKU",
                riskLevel = 2
            )
            lower.contains("reboot") -> ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "REBOOT_DEVICE",
                parameters = emptyMap(),
                requiredCapability = "DHIZUKU",
                riskLevel = 3
            )
            lower.contains("status") || lower.contains("capability") -> ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "SHOW_STATUS",
                parameters = emptyMap(),
                requiredCapability = "STANDARD",
                riskLevel = 0
            )
            else -> ActionRequest(
                id = UUID.randomUUID().toString(),
                actionType = "UNKNOWN",
                parameters = mapOf("raw_input" to input),
                requiredCapability = "STANDARD",
                riskLevel = 0
            )
        }
    }
}
