package com.assistant.core.services

import com.assistant.core.models.Task

class SchedulerService {

    private val queuedTasks = mutableListOf<Task>()

    fun enqueue(task: Task) {
        queuedTasks += task
    }

    fun getQueuedTasks(): List<Task> = queuedTasks.toList()
}
