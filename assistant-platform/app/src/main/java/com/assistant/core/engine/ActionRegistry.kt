package com.assistant.core.engine

data class ActionDefinition(
    val actionType: String,
    val defaultCapability: String,
    val defaultRiskLevel: Int,
    val description: String
)

class ActionRegistry {

    private val actions = mutableMapOf<String, ActionDefinition>()

    init {
        register(ActionDefinition("CREATE_PROJECT", "STANDARD", 1, "Create a new coding project"))
        register(ActionDefinition("WRITE_FILE", "STANDARD", 1, "Write a text file"))
        register(ActionDefinition("SHOW_STATUS", "STANDARD", 0, "Show system and capability status"))
        register(ActionDefinition("RUN_SHELL", "SHIZUKU", 2, "Run a shell command via Shizuku"))
        register(ActionDefinition("REBOOT_DEVICE", "DHIZUKU", 3, "Reboot the device via Dhizuku"))
    }

    fun register(definition: ActionDefinition) {
        actions[definition.actionType] = definition
    }

    fun getAction(actionType: String): ActionDefinition? = actions[actionType]

    fun getAllActions(): Map<String, ActionDefinition> = actions.toMap()

    fun isRegistered(actionType: String): Boolean = actions.containsKey(actionType)
}
