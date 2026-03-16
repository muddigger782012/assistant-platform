package com.assistant.core.storage

import android.content.ContentValues
import com.assistant.core.models.ActionResult

data class AuditEntry(
    val id: String,
    val actionId: String?,
    val adapterUsed: String,
    val status: String,
    val message: String,
    val createdAt: Long
)

class AuditRepository(private val database: Database) {

    fun insert(result: ActionResult) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("id", result.id)
            put("action_id", result.id)
            put("adapter_used", result.adapterUsed)
            put("status", if (result.success) "SUCCESS" else "FAILED")
            put("message", result.message)
            put("created_at", System.currentTimeMillis())
        }
        db.insert("audit_logs", null, values)
    }

    fun getRecent(limit: Int = 50): List<AuditEntry> {
        val db = database.readableDatabase
        val cursor = db.query(
            "audit_logs",
            arrayOf("id", "action_id", "adapter_used", "status", "message", "created_at"),
            null, null, null, null, "created_at DESC", limit.toString()
        )
        return cursor.use {
            val list = mutableListOf<AuditEntry>()
            while (it.moveToNext()) {
                list.add(AuditEntry(
                    id = it.getString(0),
                    actionId = it.getString(1),
                    adapterUsed = it.getString(2),
                    status = it.getString(3),
                    message = it.getString(4),
                    createdAt = it.getLong(5)
                ))
            }
            list
        }
    }
}
