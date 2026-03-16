package com.assistant.core.services

import android.content.Context
import com.assistant.core.models.ActionResult
import com.assistant.core.storage.AuditRepository

class AuditService(context: Context) {

    private val auditRepository = AuditRepository(context)

    fun log(actionId: String, result: ActionResult) {
        auditRepository.insert(actionId, result)
    }

    fun getRecentEntries(limit: Int = 20): List<Map<String, String>> {
        return auditRepository.getRecent(limit)
    }

    fun formatRecentLogs(): String {
        val entries = getRecentEntries()
        if (entries.isEmpty()) return "No audit log entries yet."
        return entries.joinToString("\n") { entry ->
            "[${entry["status"]}] ${entry["adapter_used"]}: ${entry["message"]}"
        }
    }
}
