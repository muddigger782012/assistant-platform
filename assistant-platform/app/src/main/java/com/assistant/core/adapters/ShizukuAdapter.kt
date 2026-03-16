package com.assistant.core.adapters

import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import rikka.shizuku.Shizuku
import java.util.UUID

class ShizukuAdapter {

    companion object {
        const val ADAPTER_NAME = "ShizukuAdapter"
        private const val SHIZUKU_CODE = 1001
    }

    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                false
            } else {
                Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            false
        }
    }

    fun execute(request: ActionRequest): ActionResult {
        if (!isAvailable()) {
            return ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = ADAPTER_NAME,
                message = "Shizuku is not available on this device. Please install and start Shizuku."
            )
        }

        if (!hasPermission()) {
            return ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = ADAPTER_NAME,
                message = "Shizuku permission not granted. Please grant permission in the Shizuku app."
            )
        }

        return when (request.actionType) {
            "RUN_SHELL" -> runShell(request)
            else -> ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = ADAPTER_NAME,
                message = "Unsupported Shizuku action: ${request.actionType}"
            )
        }
    }

    private fun runShell(request: ActionRequest): ActionResult {
        val command = request.parameters["command"]?.toString() ?: "id"
        return ActionResult(
            id = UUID.randomUUID().toString(),
            success = true,
            adapterUsed = ADAPTER_NAME,
            message = "Shizuku shell command submitted: $command",
            output = "[Shizuku] Command queued: $command (real execution requires IPC setup)"
        )
    }
}
