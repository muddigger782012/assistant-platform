package com.assistant.core.services

import com.assistant.core.storage.AuditLogEntry
import com.assistant.core.storage.AuditRepository

class AuditService(private val auditRepository: AuditRepository) {

    fun logActionResult(actionId: String?, adapterUsed: String?, status: String?, message: String?) {
        auditRepository.insertLog(
            actionId = actionId,
            adapterUsed = adapterUsed,
            status = status,
            message = message,
        )
    }

    fun getRecentLogs(limit: Int = 20): List<AuditLogEntry> = auditRepository.getRecentLogs(limit)
}
