package com.assistant.core.storage

import android.content.ContentValues
import android.content.Context
import com.assistant.core.models.ActionResult

class AuditRepository(context: Context) {

    private val db = Database(context).writableDatabase

    fun insert(actionId: String, result: ActionResult) {
        val values = ContentValues().apply {
            put("id", result.id)
            put("action_id", actionId)
            put("adapter_used", result.adapterUsed)
            put("status", if (result.success) "SUCCESS" else "FAILURE")
            put("message", result.message)
            put("created_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict(
            Database.TABLE_AUDIT_LOGS, null, values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getRecent(limit: Int = 20): List<Map<String, String>> {
        val entries = mutableListOf<Map<String, String>>()
        val cursor = db.query(
            Database.TABLE_AUDIT_LOGS, null, null, null, null, null,
            "created_at DESC", limit.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                entries.add(
                    mapOf(
                        "id" to (it.getString(it.getColumnIndexOrThrow("id")) ?: ""),
                        "action_id" to (it.getString(it.getColumnIndexOrThrow("action_id")) ?: ""),
                        "adapter_used" to (it.getString(it.getColumnIndexOrThrow("adapter_used")) ?: ""),
                        "status" to (it.getString(it.getColumnIndexOrThrow("status")) ?: ""),
                        "message" to (it.getString(it.getColumnIndexOrThrow("message")) ?: "")
                    )
                )
            }
        }
        return entries
    }
}
