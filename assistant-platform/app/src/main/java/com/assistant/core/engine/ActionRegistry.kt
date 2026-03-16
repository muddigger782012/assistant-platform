package com.assistant.core.engine

import com.assistant.core.models.ActionRequest

class ActionRegistry {

    data class ActionDefinition(
        val actionType: String,
        val requiredCapability: String,
        val riskLevel: Int,
        val description: String
    )

    private val registry = mapOf(
        "CREATE_PROJECT" to ActionDefinition(
            actionType = "CREATE_PROJECT",
            requiredCapability = "STANDARD",
            riskLevel = 1,
            description = "Create a local project directory with starter files"
        ),
        "RUN_SHELL" to ActionDefinition(
            actionType = "RUN_SHELL",
            requiredCapability = "SHIZUKU",
            riskLevel = 2,
            description = "Execute a shell command via Shizuku"
        ),
        "SHOW_STATUS" to ActionDefinition(
            actionType = "SHOW_STATUS",
            requiredCapability = "STANDARD",
            riskLevel = 0,
            description = "Show current capability and system status"
        ),
        "REBOOT_DEVICE" to ActionDefinition(
            actionType = "REBOOT_DEVICE",
            requiredCapability = "DHIZUKU",
            riskLevel = 3,
            description = "Reboot the device via Dhizuku or Device Owner"
        ),
        "WRITE_FILE" to ActionDefinition(
            actionType = "WRITE_FILE",
            requiredCapability = "STANDARD",
            riskLevel = 1,
            description = "Write content to a file in app storage"
        )
    )

    fun getDefinition(actionType: String): ActionDefinition? = registry[actionType]

    fun isSupported(actionType: String): Boolean = registry.containsKey(actionType)

    fun listAll(): List<ActionDefinition> = registry.values.toList()
}
