package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import java.util.UUID

data class RegisteredAction(
    val actionType: String,
    val requiredCapability: String,
    val riskLevel: Int,
    val defaultParameters: Map<String, Any?> = emptyMap(),
)

class ActionRegistry {

    private val supportedActions = mapOf(
        ACTION_CREATE_PROJECT to RegisteredAction(
            actionType = ACTION_CREATE_PROJECT,
            requiredCapability = CAPABILITY_STANDARD,
            riskLevel = 1,
            defaultParameters = mapOf("name" to "assistant_demo"),
        ),
        ACTION_RUN_SHELL to RegisteredAction(
            actionType = ACTION_RUN_SHELL,
            requiredCapability = CAPABILITY_SHIZUKU,
            riskLevel = 2,
            defaultParameters = mapOf("command" to "id"),
        ),
        ACTION_SHOW_STATUS to RegisteredAction(
            actionType = ACTION_SHOW_STATUS,
            requiredCapability = CAPABILITY_STANDARD,
            riskLevel = 0,
            defaultParameters = emptyMap(),
        ),
        ACTION_REBOOT_DEVICE to RegisteredAction(
            actionType = ACTION_REBOOT_DEVICE,
            requiredCapability = CAPABILITY_DHIZUKU,
            riskLevel = 3,
            defaultParameters = emptyMap(),
        ),
        ACTION_WRITE_FILE to RegisteredAction(
            actionType = ACTION_WRITE_FILE,
            requiredCapability = CAPABILITY_STANDARD,
            riskLevel = 1,
            defaultParameters = emptyMap(),
        ),
    )

    fun createRequest(
        actionType: String,
        parameters: Map<String, Any?> = emptyMap(),
        allowFallback: Boolean = true,
    ): ActionRequest? {
        val definition = supportedActions[actionType] ?: return null
        return ActionRequest(
            id = UUID.randomUUID().toString(),
            actionType = definition.actionType,
            parameters = definition.defaultParameters + parameters,
            requiredCapability = definition.requiredCapability,
            riskLevel = definition.riskLevel,
            allowFallback = allowFallback,
        )
    }

    fun getSupportedActionTypes(): Set<String> = supportedActions.keys

    companion object {
        const val ACTION_CREATE_PROJECT = "CREATE_PROJECT"
        const val ACTION_RUN_SHELL = "RUN_SHELL"
        const val ACTION_SHOW_STATUS = "SHOW_STATUS"
        const val ACTION_REBOOT_DEVICE = "REBOOT_DEVICE"
        const val ACTION_WRITE_FILE = "WRITE_FILE"

        const val CAPABILITY_STANDARD = "STANDARD"
        const val CAPABILITY_SPECIAL_ACCESS = "SPECIAL_ACCESS"
        const val CAPABILITY_SHIZUKU = "SHIZUKU"
        const val CAPABILITY_DHIZUKU = "DHIZUKU"
    }
}
