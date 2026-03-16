package com.assistant.core.adapters

import android.content.pm.PackageManager
import com.assistant.core.engine.ActionAdapter
import com.assistant.core.engine.ActionRegistry
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import rikka.shizuku.Shizuku

class ShizukuAdapter : ActionAdapter {

    override val adapterName: String = "ShizukuAdapter"

    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            isAvailable() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    override fun supports(actionType: String): Boolean = actionType == ActionRegistry.ACTION_RUN_SHELL

    override fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        if (!isAvailable()) {
            return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = adapterName,
                message = "Shizuku is unavailable on this device.",
            )
        }

        if (!hasPermission()) {
            return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = adapterName,
                message = "Shizuku is available but permission is not granted.",
            )
        }

        val command = request.parameters["command"]?.toString()?.ifBlank { "id" } ?: "id"
        return ActionResult(
            id = request.id,
            success = true,
            adapterUsed = adapterName,
            message = "Shizuku permission is granted. Shell execution is a safe MVP placeholder.",
            output = "Requested shell command: $command",
        )
    }
}
