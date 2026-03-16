package com.assistant.core.services

import com.assistant.core.models.ActionResult
import com.assistant.core.storage.AuditRepository
import com.assistant.core.storage.AuditEntry

class AuditService(private val auditRepository: AuditRepository) {

    fun logResult(actionId: String?, result: ActionResult) {
        auditRepository.logAudit(
            actionId = actionId,
            adapterUsed = result.adapterUsed,
            status = if (result.success) "success" else "failure",
            message = result.message
        )
    }

    fun getRecentEntries(limit: Int = 20): List<AuditEntry> {
        return auditRepository.getRecentLogs(limit)
    }
}
