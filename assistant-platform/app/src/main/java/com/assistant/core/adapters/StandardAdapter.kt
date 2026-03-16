package com.assistant.core.adapters

import android.content.Context
import com.assistant.core.engine.IntentClassifier
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.Project
import com.assistant.core.services.CodingService
import com.assistant.core.services.SystemService
import com.assistant.core.storage.Database
import com.assistant.core.storage.ProjectRepository
import java.util.UUID

class StandardAdapter(private val context: Context) {

    private val codingService = CodingService(context)
    private val systemService = SystemService(context)
    private val projectRepository = ProjectRepository(Database(context))

    fun execute(request: ActionRequest): ActionResult {
        return when (request.actionType) {
            IntentClassifier.CREATE_PROJECT -> executeCreateProject(request)
            IntentClassifier.SHOW_STATUS -> executeShowStatus(request)
            else -> ActionResult(
                id = request.id,
                success = false,
                adapterUsed = "StandardAdapter",
                message = "Unsupported action: ${request.actionType}"
            )
        }
    }

    private fun executeCreateProject(request: ActionRequest): ActionResult {
        val name = request.parameters["name"] as? String ?: "assistant_demo"
        return try {
            val projectDir = codingService.createProject(name)
            val project = Project(
                id = UUID.randomUUID().toString(),
                name = name,
                rootPath = projectDir.absolutePath,
                createdAt = System.currentTimeMillis()
            )
            projectRepository.insert(project)
            ActionResult(
                id = request.id,
                success = true,
                adapterUsed = "StandardAdapter",
                message = "Project created: $name",
                output = projectDir.absolutePath
            )
        } catch (e: Exception) {
            ActionResult(
                id = request.id,
                success = false,
                adapterUsed = "StandardAdapter",
                message = "Failed to create project: ${e.message}"
            )
        }
    }

    private fun executeShowStatus(request: ActionRequest): ActionResult {
        val capabilityState = com.assistant.core.engine.CapabilityDetector(context).detect()
        val summary = systemService.produceStatusSummary(capabilityState)
        return ActionResult(
            id = request.id,
            success = true,
            adapterUsed = "StandardAdapter",
            message = "Status retrieved",
            output = summary
        )
    }
}
