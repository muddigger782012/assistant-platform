package com.assistant.core.services

import com.assistant.core.models.Task
import java.util.UUID

class SchedulerService {

    private val tasks = mutableListOf<Task>()

    fun storeTask(title: String, status: String = "PENDING"): Task {
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            status = status,
            createdAt = System.currentTimeMillis()
        )
        tasks.add(task)
        return task
    }

    fun getTasks(): List<Task> = tasks.toList()
}
