package com.assistant.core.storage

import android.content.ContentValues
import java.util.UUID

class AuditRepository(private val database: Database) {

    fun logAudit(actionId: String?, adapterUsed: String?, status: String?, message: String?) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("id", UUID.randomUUID().toString())
            put("action_id", actionId)
            put("adapter_used", adapterUsed)
            put("status", status)
            put("message", message)
            put("created_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("audit_logs", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getRecentLogs(limit: Int = 20): List<AuditEntry> {
        val db = database.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, action_id, adapter_used, status, message, created_at FROM audit_logs ORDER BY created_at DESC LIMIT ?",
            arrayOf(limit.toString())
        )
        val entries = mutableListOf<AuditEntry>()
        cursor.use {
            while (it.moveToNext()) {
                entries.add(
                    AuditEntry(
                        id = it.getString(0),
                        actionId = it.getString(1),
                        adapterUsed = it.getString(2),
                        status = it.getString(3),
                        message = it.getString(4),
                        createdAt = it.getLong(5)
                    )
                )
            }
        }
        return entries
    }
}

data class AuditEntry(
    val id: String,
    val actionId: String?,
    val adapterUsed: String?,
    val status: String?,
    val message: String?,
    val createdAt: Long
)
