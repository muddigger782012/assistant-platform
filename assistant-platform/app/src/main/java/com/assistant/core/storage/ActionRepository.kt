package com.assistant.core.storage

import android.content.ContentValues
import com.assistant.core.models.ActionRequest

class ActionRepository(private val database: Database) {

    fun insertAction(request: ActionRequest) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("id", request.id)
            put("action_type", request.actionType)
            put("parameters", request.parameters.toString())
            put("required_capability", request.requiredCapability)
            put("risk_level", request.riskLevel)
            put("created_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("actions", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun enqueueAction(id: String, actionId: String, status: String, scheduledTime: Long?) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("id", id)
            put("action_id", actionId)
            put("status", status)
            put("scheduled_time", scheduledTime)
        }
        db.insertWithOnConflict("action_queue", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }
}
