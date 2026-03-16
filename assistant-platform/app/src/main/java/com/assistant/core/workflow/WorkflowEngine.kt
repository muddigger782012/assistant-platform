package com.assistant.core.workflow

import com.assistant.core.models.Workflow
import com.assistant.core.storage.Database
import android.content.ContentValues
import java.util.UUID

class WorkflowEngine(private val database: Database) {

    private val workflows = mutableListOf<Workflow>()

    fun createWorkflow(name: String, triggerType: String): Workflow {
        val workflow = Workflow(
            id = UUID.randomUUID().toString(),
            name = name,
            triggerType = triggerType,
            createdAt = System.currentTimeMillis()
        )
        workflows.add(workflow)
        persistWorkflow(workflow)
        return workflow
    }

    fun getWorkflows(): List<Workflow> = workflows.toList()

    private fun persistWorkflow(workflow: Workflow) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("id", workflow.id)
            put("name", workflow.name)
            put("trigger_type", workflow.triggerType)
            put("created_at", workflow.createdAt)
        }
        db.insertWithOnConflict("workflows", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }
}
