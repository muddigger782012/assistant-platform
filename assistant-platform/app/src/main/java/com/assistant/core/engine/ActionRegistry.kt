package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import java.util.UUID

object ActionRegistry {

    fun createProject(name: String = "assistant_demo"): ActionRequest = ActionRequest(
        id = UUID.randomUUID().toString(),
        actionType = IntentClassifier.CREATE_PROJECT,
        parameters = mapOf("name" to name),
        requiredCapability = "STANDARD",
        riskLevel = 1
    )

    fun runShell(command: String = "id"): ActionRequest = ActionRequest(
        id = UUID.randomUUID().toString(),
        actionType = IntentClassifier.RUN_SHELL,
        parameters = mapOf("command" to command),
        requiredCapability = "SHIZUKU",
        riskLevel = 2
    )

    fun showStatus(): ActionRequest = ActionRequest(
        id = UUID.randomUUID().toString(),
        actionType = IntentClassifier.SHOW_STATUS,
        parameters = emptyMap(),
        requiredCapability = "STANDARD",
        riskLevel = 0
    )

    fun rebootDevice(): ActionRequest = ActionRequest(
        id = UUID.randomUUID().toString(),
        actionType = IntentClassifier.REBOOT_DEVICE,
        parameters = emptyMap(),
        requiredCapability = "DHIZUKU",
        riskLevel = 3
    )

    fun fromRawInput(input: String): ActionRequest? {
        val actionType = IntentClassifier.classify(input)
        return when (actionType) {
            IntentClassifier.CREATE_PROJECT -> createProject()
            IntentClassifier.RUN_SHELL -> runShell()
            IntentClassifier.SHOW_STATUS -> showStatus()
            IntentClassifier.REBOOT_DEVICE -> rebootDevice()
            else -> null
        }
    }
}
