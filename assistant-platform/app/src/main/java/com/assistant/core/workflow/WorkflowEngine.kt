package com.assistant.core.workflow

import com.assistant.core.models.Task
import com.assistant.core.models.Workflow

class WorkflowEngine {

    private val workflows = mutableListOf<Workflow>()
    private val tasks = mutableListOf<Task>()

    fun registerWorkflow(workflow: Workflow) {
        workflows.add(workflow)
    }

    fun addTask(task: Task) {
        tasks.add(task)
    }

    fun getWorkflows(): List<Workflow> = workflows.toList()

    fun getTasks(): List<Task> = tasks.toList()

    fun getPendingTasks(): List<Task> = tasks.filter { it.status == "PENDING" }
}
