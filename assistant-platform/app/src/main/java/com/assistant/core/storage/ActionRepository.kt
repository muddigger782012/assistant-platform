package com.assistant.core.storage

import android.content.ContentValues
import android.content.Context
import com.assistant.core.models.ActionRequest

class ActionRepository(context: Context) {

    private val db = Database(context).writableDatabase

    fun insert(request: ActionRequest) {
        val values = ContentValues().apply {
            put("id", request.id)
            put("action_type", request.actionType)
            put("parameters", request.parameters.entries.joinToString(",") { "${it.key}=${it.value}" })
            put("required_capability", request.requiredCapability)
            put("risk_level", request.riskLevel)
            put("created_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict(
            Database.TABLE_ACTIONS, null, values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getAll(): List<Map<String, String>> {
        val actions = mutableListOf<Map<String, String>>()
        val cursor = db.query(Database.TABLE_ACTIONS, null, null, null, null, null, "created_at DESC")
        cursor.use {
            while (it.moveToNext()) {
                actions.add(
                    mapOf(
                        "id" to (it.getString(it.getColumnIndexOrThrow("id")) ?: ""),
                        "action_type" to (it.getString(it.getColumnIndexOrThrow("action_type")) ?: ""),
                        "required_capability" to (it.getString(it.getColumnIndexOrThrow("required_capability")) ?: "")
                    )
                )
            }
        }
        return actions
    }
}
