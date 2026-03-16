package com.assistant.core.engine

import android.content.Context
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import com.assistant.core.services.AuditService
import com.assistant.core.storage.ActionRepository
import java.util.UUID

class AssistantEngine(private val context: Context) {

    private val capabilityDetector = CapabilityDetector(context)
    private val intentClassifier = IntentClassifier()
    private val auditService = AuditService(context)
    private val actionRepository = ActionRepository(context)

    var capabilityState: CapabilityState = CapabilityState(
        standard = true,
        specialAccess = false,
        shizuku = false,
        dhizuku = false,
        deviceOwner = false,
        oemPrivileged = false,
        platformSigned = false
    )
        private set

    fun initialize() {
        capabilityState = capabilityDetector.detect()
    }

    fun processCommand(input: String): ActionResult {
        val request = intentClassifier.classify(input)
        return executeRequest(request)
    }

    fun executeRequest(request: ActionRequest): ActionResult {
        actionRepository.insert(request)
        val executor = ActionExecutor(context, capabilityState)
        val result = executor.execute(request)
        auditService.log(request.id, result)
        return result
    }

    fun currentCapabilityState(): CapabilityState = capabilityState

    fun getRecentAuditLog(): String = auditService.formatRecentLogs()
}
