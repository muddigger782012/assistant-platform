package com.assistant.core.workflow

import com.assistant.core.models.Workflow
import java.util.UUID

class WorkflowEngine {

    fun createWorkflow(name: String, triggerType: String): Workflow = Workflow(
        id = UUID.randomUUID().toString(),
        name = name,
        triggerType = triggerType,
        createdAt = System.currentTimeMillis()
    )

    fun getWorkflows(): List<Workflow> = emptyList()
}
