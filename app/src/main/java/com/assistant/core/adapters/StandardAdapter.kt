package com.assistant.core.adapters

import com.assistant.core.engine.ActionAdapter
import com.assistant.core.engine.ActionRegistry
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import com.assistant.core.models.Project
import com.assistant.core.services.CodingService
import com.assistant.core.services.FileService
import com.assistant.core.services.SystemService
import com.assistant.core.storage.ProjectRepository
import java.io.File
import java.util.UUID

class StandardAdapter(
    private val codingService: CodingService,
    private val fileService: FileService,
    private val systemService: SystemService,
    private val projectRepository: ProjectRepository,
) : ActionAdapter {

    override val adapterName: String = "StandardAdapter"

    override fun supports(actionType: String): Boolean {
        return actionType in setOf(
            ActionRegistry.ACTION_CREATE_PROJECT,
            ActionRegistry.ACTION_WRITE_FILE,
            ActionRegistry.ACTION_SHOW_STATUS,
        )
    }

    override fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        return when (request.actionType) {
            ActionRegistry.ACTION_CREATE_PROJECT -> createProject(request)
            ActionRegistry.ACTION_WRITE_FILE -> writeFile(request)
            ActionRegistry.ACTION_SHOW_STATUS -> showStatus(request, capabilityState)
            else -> ActionResult(
                id = request.id,
                success = false,
                adapterUsed = adapterName,
                message = "Unsupported action ${request.actionType}.",
            )
        }
    }

    private fun createProject(request: ActionRequest): ActionResult {
        val projectName = request.parameters["name"]?.toString()?.ifBlank { "assistant_demo" } ?: "assistant_demo"
        val projectRoot = codingService.createProject(projectName)
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = projectName,
            rootPath = projectRoot.absolutePath,
            createdAt = System.currentTimeMillis(),
        )
        projectRepository.insertProject(project)
        fileService.listProjectFiles(projectRoot).forEach { file ->
            projectRepository.insertProjectFile(
                projectId = project.id,
                path = file.relativeTo(projectRoot).path,
                lastModified = file.lastModified(),
            )
        }
        return ActionResult(
            id = request.id,
            success = true,
            adapterUsed = adapterName,
            message = "Project $projectName created successfully.",
            output = projectRoot.absolutePath,
        )
    }

    private fun writeFile(request: ActionRequest): ActionResult {
        val targetPath = request.parameters["path"]?.toString()?.trim().orEmpty()
        if (targetPath.isBlank()) {
            return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = adapterName,
                message = "WRITE_FILE requires a path parameter.",
            )
        }
        val content = request.parameters["content"]?.toString().orEmpty()
        val root = fileService.ensureDirectoriesExist("")
        val outputFile = File(root, targetPath)
        fileService.writeTextFile(outputFile, content)
        return ActionResult(
            id = request.id,
            success = true,
            adapterUsed = adapterName,
            message = "Wrote file to ${outputFile.absolutePath}.",
            output = outputFile.absolutePath,
        )
    }

    private fun showStatus(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        val summary = systemService.buildStatusSummary(capabilityState)
        return ActionResult(
            id = request.id,
            success = true,
            adapterUsed = adapterName,
            message = "Capability status loaded.",
            output = summary,
        )
    }
}
