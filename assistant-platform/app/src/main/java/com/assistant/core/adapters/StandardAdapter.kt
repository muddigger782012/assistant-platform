package com.assistant.core.adapters

import android.content.Context
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.services.CodingService
import com.assistant.core.services.FileService
import com.assistant.core.services.SystemService
import com.assistant.core.models.CapabilityState
import java.util.UUID

class StandardAdapter(private val context: Context) {

    private val fileService = FileService(context)
    private val codingService = CodingService(context, fileService)
    private val systemService = SystemService(context)

    fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        return when (request.actionType) {
            "CREATE_PROJECT" -> handleCreateProject(request)
            "WRITE_FILE" -> handleWriteFile(request)
            "SHOW_STATUS" -> handleShowStatus(request, capabilityState)
            else -> ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "standard",
                message = "Unsupported action type: ${request.actionType}"
            )
        }
    }

    private fun handleCreateProject(request: ActionRequest): ActionResult {
        val projectName = request.parameters["name"] as? String ?: "assistant_demo"
        return try {
            val projectDir = codingService.createProject(projectName)
            ActionResult(
                id = UUID.randomUUID().toString(),
                success = true,
                adapterUsed = "standard",
                message = "Project '$projectName' created successfully",
                output = "Project root: ${projectDir.absolutePath}"
            )
        } catch (e: Exception) {
            ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "standard",
                message = "Failed to create project: ${e.message}"
            )
        }
    }

    private fun handleWriteFile(request: ActionRequest): ActionResult {
        val path = request.parameters["path"] as? String ?: return ActionResult(
            id = UUID.randomUUID().toString(),
            success = false,
            adapterUsed = "standard",
            message = "Missing 'path' parameter"
        )
        val content = request.parameters["content"] as? String ?: ""
        return try {
            fileService.writeTextFile(path, content)
            ActionResult(
                id = UUID.randomUUID().toString(),
                success = true,
                adapterUsed = "standard",
                message = "File written: $path"
            )
        } catch (e: Exception) {
            ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "standard",
                message = "Failed to write file: ${e.message}"
            )
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleShowStatus(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        val status = systemService.getStatusSummary(capabilityState)
        return ActionResult(
            id = UUID.randomUUID().toString(),
            success = true,
            adapterUsed = "standard",
            message = "Status retrieved",
            output = status
        )
    }
}
