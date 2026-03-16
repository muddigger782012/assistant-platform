package com.assistant.core.storage

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import java.util.UUID

data class AuditLogEntry(
    val id: String,
    val actionId: String?,
    val adapterUsed: String?,
    val status: String?,
    val message: String?,
    val createdAt: Long,
)

class AuditRepository(private val database: Database) {

    fun insertLog(
        actionId: String?,
        adapterUsed: String?,
        status: String?,
        message: String?,
        createdAt: Long = System.currentTimeMillis(),
    ) {
        val values = ContentValues().apply {
            put("id", UUID.randomUUID().toString())
            put("action_id", actionId)
            put("adapter_used", adapterUsed)
            put("status", status)
            put("message", message)
            put("created_at", createdAt)
        }
        database.writableDatabase.insertWithOnConflict(
            Database.TABLE_AUDIT_LOGS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun getRecentLogs(limit: Int = 20): List<AuditLogEntry> {
        val entries = mutableListOf<AuditLogEntry>()
        val safeLimit = limit.coerceAtLeast(1)
        database.readableDatabase.query(
            Database.TABLE_AUDIT_LOGS,
            arrayOf("id", "action_id", "adapter_used", "status", "message", "created_at"),
            null,
            null,
            null,
            null,
            "created_at DESC",
            safeLimit.toString(),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                entries += AuditLogEntry(
                    id = cursor.getString(0),
                    actionId = cursor.getString(1),
                    adapterUsed = cursor.getString(2),
                    status = cursor.getString(3),
                    message = cursor.getString(4),
                    createdAt = cursor.getLong(5),
                )
            }
        }
        return entries
    }
}
