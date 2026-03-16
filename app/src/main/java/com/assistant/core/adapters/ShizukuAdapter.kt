package com.assistant.core.adapters

import android.content.pm.PackageManager
import com.assistant.core.engine.ActionRegistry
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import dev.rikka.shizuku.Shizuku

class ShizukuAdapter {

    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    fun execute(actionRequest: ActionRequest, capabilityState: CapabilityState): ActionResult {
        if (actionRequest.actionType != ActionRegistry.RUN_SHELL) {
            return ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "SHIZUKU",
                message = "Unsupported action for ShizukuAdapter: ${actionRequest.actionType}"
            )
        }

        if (!capabilityState.shizuku || !isAvailable()) {
            return ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "SHIZUKU",
                message = "Shizuku unavailable on this device."
            )
        }

        if (!hasPermission()) {
            return ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "SHIZUKU",
                message = "Shizuku permission not granted. Please allow in Shizuku manager."
            )
        }

        val command = actionRequest.parameters["command"] as? String ?: "id"
        return ActionResult(
            id = actionRequest.id,
            success = true,
            adapterUsed = "SHIZUKU",
            message = "Shizuku test command handled (placeholder execution).",
            output = "command=$command"
        )
    }
}
