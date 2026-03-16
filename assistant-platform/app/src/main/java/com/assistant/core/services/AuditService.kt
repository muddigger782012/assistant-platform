package com.assistant.core.services

import android.content.Context
import com.assistant.core.models.ActionResult
import com.assistant.core.storage.AuditRepository
import com.assistant.core.storage.Database

class AuditService(context: Context) {

    private val auditRepository = AuditRepository(Database(context))

    fun logActionResult(result: ActionResult) {
        auditRepository.insert(result)
    }

    fun loadRecentAuditEntries(limit: Int = 20): List<String> {
        return auditRepository.getRecent(limit).map { entry ->
            "[${entry.status}] ${entry.adapterUsed}: ${entry.message}"
        }
    }
}
