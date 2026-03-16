package com.assistant.core.services

import com.assistant.core.models.Task

class SchedulerService {

    private val pendingTasks = mutableListOf<Task>()

    fun scheduleTask(task: Task) {
        pendingTasks.add(task)
    }

    fun getPendingTasks(): List<Task> = pendingTasks.toList()

    fun clearCompleted() {
        pendingTasks.removeAll { it.status == "COMPLETED" }
    }
}
