package com.assistant.core.workflow

import com.assistant.core.models.Task
import com.assistant.core.models.Workflow

class WorkflowEngine {

    private val workflows = mutableListOf<Workflow>()
    private val tasks = mutableListOf<Task>()

    fun registerWorkflow(workflow: Workflow) {
        workflows += workflow
    }

    fun addTask(task: Task) {
        tasks += task
    }

    fun getWorkflows(): List<Workflow> = workflows.toList()

    fun getTasks(): List<Task> = tasks.toList()
}
