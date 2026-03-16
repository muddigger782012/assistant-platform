package com.assistant.core.storage

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.assistant.core.models.ActionRequest
import org.json.JSONObject
import java.util.UUID

class ActionRepository(private val database: Database) {

    fun insertAction(actionRequest: ActionRequest, createdAt: Long = System.currentTimeMillis()) {
        val values = ContentValues().apply {
            put("id", actionRequest.id)
            put("action_type", actionRequest.actionType)
            put("parameters", JSONObject(actionRequest.parameters).toString())
            put("required_capability", actionRequest.requiredCapability)
            put("risk_level", actionRequest.riskLevel)
            put("created_at", createdAt)
        }
        database.writableDatabase.insertWithOnConflict(
            Database.TABLE_ACTIONS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun enqueueAction(actionId: String, status: String, scheduledTime: Long? = null) {
        val values = ContentValues().apply {
            put("id", UUID.randomUUID().toString())
            put("action_id", actionId)
            put("status", status)
            put("scheduled_time", scheduledTime)
        }
        database.writableDatabase.insertWithOnConflict(
            Database.TABLE_ACTION_QUEUE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }
}
