package com.assistant.core.storage

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
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
        db.insert("actions", null, values)
    }

    fun getById(id: String): ActionRequest? {
        val db = database.readableDatabase
        val cursor = db.query(
            "actions",
            arrayOf("id", "action_type", "parameters", "required_capability", "risk_level", "created_at"),
            "id = ?",
            arrayOf(id),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                ActionRequest(
                    id = it.getString(0),
                    actionType = it.getString(1),
                    parameters = emptyMap(),
                    requiredCapability = it.getString(3),
                    riskLevel = it.getInt(4)
                )
            } else null
        }
    }
}
