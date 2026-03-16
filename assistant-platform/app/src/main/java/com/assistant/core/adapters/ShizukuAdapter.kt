package com.assistant.core.adapters

import android.content.Context
import com.assistant.core.engine.IntentClassifier
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import rikka.shizuku.Shizuku

class ShizukuAdapter(private val context: Context) {

    fun execute(request: ActionRequest): ActionResult {
        if (!isAvailable()) {
            return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = "ShizukuAdapter",
                message = "Shizuku is not available. Install Shizuku app and start the server."
            )
        }

        return when (request.actionType) {
            IntentClassifier.RUN_SHELL -> executeRunShell(request)
            else -> ActionResult(
                id = request.id,
                success = false,
                adapterUsed = "ShizukuAdapter",
                message = "Unsupported action: ${request.actionType}"
            )
        }
    }

    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    private fun executeRunShell(request: ActionRequest): ActionResult {
        val command = request.parameters["command"] as? String ?: "id"
        return ActionResult(
            id = request.id,
            success = true,
            adapterUsed = "ShizukuAdapter",
            message = "Shizuku available. Test command '$command'",
            output = "Shizuku is running. Shell execution requires UserService (not implemented in MVP)."
        )
    }
}
