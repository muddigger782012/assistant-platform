package com.assistant.core.adapters

import android.content.Context
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.services.CodingService
import com.assistant.core.services.SystemService
import com.assistant.core.models.CapabilityState
import java.io.File
import java.util.UUID

class StandardAdapter(
    private val context: Context,
    private val capabilityState: CapabilityState
) {

    companion object {
        const val ADAPTER_NAME = "StandardAdapter"
    }

    private val codingService = CodingService(File(context.filesDir, "projects"))
    private val systemService = SystemService(context.filesDir.absolutePath)

    fun execute(request: ActionRequest): ActionResult {
        return when (request.actionType) {
            "CREATE_PROJECT" -> createProject(request)
            "SHOW_STATUS" -> showStatus(request)
            "WRITE_FILE" -> writeFile(request)
            else -> ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = ADAPTER_NAME,
                message = "Unsupported action: ${request.actionType}"
            )
        }
    }

    private fun createProject(request: ActionRequest): ActionResult {
        val name = request.parameters["name"]?.toString() ?: "assistant_demo"
        return try {
            val projectDir = codingService.createProject(name)
            ActionResult(
                id = UUID.randomUUID().toString(),
                success = true,
                adapterUsed = ADAPTER_NAME,
                message = "Project '$name' created successfully",
                output = projectDir.absolutePath
            )
        } catch (e: Exception) {
            ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = ADAPTER_NAME,
                message = "Failed to create project: ${e.message}"
            )
        }
    }

    private fun showStatus(request: ActionRequest): ActionResult {
        val summary = systemService.getStatusSummary(capabilityState)
        return ActionResult(
            id = UUID.randomUUID().toString(),
            success = true,
            adapterUsed = ADAPTER_NAME,
            message = "Status retrieved",
            output = summary
        )
    }

    private fun writeFile(request: ActionRequest): ActionResult {
        val path = request.parameters["path"]?.toString()
            ?: return ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = ADAPTER_NAME,
                message = "Missing parameter: path"
            )
        val content = request.parameters["content"]?.toString() ?: ""
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            ActionResult(
                id = UUID.randomUUID().toString(),
                success = true,
                adapterUsed = ADAPTER_NAME,
                message = "File written: $path"
            )
        } catch (e: Exception) {
            ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = ADAPTER_NAME,
                message = "Failed to write file: ${e.message}"
            )
        }
    }
}
