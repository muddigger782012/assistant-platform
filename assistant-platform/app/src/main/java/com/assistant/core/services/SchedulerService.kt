package com.assistant.core.services

import com.assistant.core.models.Task
import com.assistant.core.storage.Database
import android.content.ContentValues
import java.util.UUID

class SchedulerService(private val database: Database) {

    private val pendingTasks = mutableListOf<Task>()

    fun scheduleTask(title: String): Task {
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            status = "pending",
            createdAt = System.currentTimeMillis()
        )
        pendingTasks.add(task)
        persistTask(task)
        return task
    }

    fun getPendingTasks(): List<Task> = pendingTasks.toList()

    private fun persistTask(task: Task) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("id", task.id)
            put("title", task.title)
            put("status", task.status)
            put("created_at", task.createdAt)
        }
        db.insertWithOnConflict("tasks", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }
}
