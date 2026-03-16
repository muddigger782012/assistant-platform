package com.assistant.core.adapters

import android.content.Context
import com.assistant.core.engine.ActionRegistry
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import com.assistant.core.services.ShizukuShellService

class ShizukuAdapter(
    context: Context
) {

    private val shellService = ShizukuShellService(context)

    fun execute(actionRequest: ActionRequest, _capabilityState: CapabilityState): ActionResult {
        if (actionRequest.actionType != ActionRegistry.RUN_SHELL) {
            return ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "SHIZUKU",
                message = "Unsupported action for ShizukuAdapter: ${actionRequest.actionType}"
            )
        }

        val command = actionRequest.parameters["command"] as? String ?: "id"
        val parsed = shellService.parseRishCommand(command)

        if (parsed.interactiveOnly) {
            val helperPath = shellService.ensureRishHelperFile().absolutePath
            return ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "SHIZUKU",
                message = "Interactive .rish shell is not available in-app. Use '.rish -c \"<command>\"'.",
                output = "rish helper file: $helperPath"
            )
        }

        return try {
            val shellResult = shellService.run(parsed)
            val message = if (shellResult.success) {
                if (shellResult.usedRishMode) {
                    "Shizuku command executed via .rish mode."
                } else {
                    "Shizuku command executed."
                }
            } else {
                "Shizuku command failed with exit code ${shellResult.exitCode}."
            }
            ActionResult(
                id = actionRequest.id,
                success = shellResult.success,
                adapterUsed = "SHIZUKU",
                message = message,
                output = buildString {
                    appendLine("command: ${shellResult.command}")
                    appendLine("rishMode: ${shellResult.usedRishMode}")
                    appendLine("rishHelper: ${shellResult.helperPath}")
                    appendLine("exitCode: ${shellResult.exitCode}")
                    if (shellResult.stdout.isNotBlank()) {
                        appendLine("stdout:")
                        appendLine(shellResult.stdout.trim())
                    }
                    if (shellResult.stderr.isNotBlank()) {
                        appendLine("stderr:")
                        appendLine(shellResult.stderr.trim())
                    }
                }.trim()
            )
        } catch (error: SecurityException) {
            val helperPath = shellService.ensureRishHelperFile().absolutePath
            ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "SHIZUKU",
                message = "Shizuku permission denied. Open Shizuku manager and grant this app permission.",
                output = "rish helper file: $helperPath"
            )
        } catch (error: Throwable) {
            val helperPath = shellService.ensureRishHelperFile().absolutePath
            ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "SHIZUKU",
                message = "Shizuku execution error: ${error.message ?: "unknown error"}",
                output = "rish helper file: $helperPath"
            )
        }
    }
}
