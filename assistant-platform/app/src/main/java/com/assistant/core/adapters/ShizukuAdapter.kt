package com.assistant.core.adapters

import android.content.pm.PackageManager
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import java.util.UUID

class ShizukuAdapter {

    fun isAvailable(): Boolean {
        return try {
            rikka.shizuku.Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (!isAvailable()) return false
            rikka.shizuku.Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermission(requestCode: Int) {
        try {
            if (isAvailable()) {
                rikka.shizuku.Shizuku.requestPermission(requestCode)
            }
        } catch (e: Exception) {
            // Shizuku not available
        }
    }

    fun execute(request: ActionRequest): ActionResult {
        if (!isAvailable()) {
            return ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "shizuku",
                message = "Shizuku is not available. Please install and start Shizuku."
            )
        }

        if (!hasPermission()) {
            return ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "shizuku",
                message = "Shizuku permission not granted. Please grant permission first."
            )
        }

        return when (request.actionType) {
            "RUN_SHELL" -> handleRunShell(request)
            else -> ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "shizuku",
                message = "Unsupported action type for Shizuku: ${request.actionType}"
            )
        }
    }

    private fun handleRunShell(request: ActionRequest): ActionResult {
        val command = request.parameters["command"] as? String ?: "id"
        return ActionResult(
            id = UUID.randomUUID().toString(),
            success = true,
            adapterUsed = "shizuku",
            message = "Shizuku shell test completed",
            output = "Shizuku is available and permission granted. Command: '$command'\n(Remote shell execution via Shizuku IPC — placeholder result for MVP)"
        )
    }
}
