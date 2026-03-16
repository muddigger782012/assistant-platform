package com.assistant.core.engine

import android.content.Context
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import com.assistant.core.services.AuditService

class AssistantEngine(context: Context) {

    private val capabilityDetector = CapabilityDetector(context)
    private val actionExecutor = ActionExecutor(context)
    private val auditService = AuditService(context)

    private var _capabilityState: CapabilityState? = null
    val capabilityState: CapabilityState
        get() = _capabilityState ?: capabilityDetector.detect().also { _capabilityState = it }

    fun detectCapabilities(): CapabilityState {
        _capabilityState = capabilityDetector.detect()
        return _capabilityState!!
    }

    fun processCommand(input: String, hasExplicitConfirmation: Boolean = true): ActionResult {
        val request = ActionRegistry.fromRawInput(input)
            ?: return ActionResult(
                id = "",
                success = false,
                adapterUsed = "IntentClassifier",
                message = "Unknown or unsupported command: $input"
            )
        return execute(request, hasExplicitConfirmation)
    }

    fun execute(request: ActionRequest, hasExplicitConfirmation: Boolean = true): ActionResult {
        val result = actionExecutor.execute(request, capabilityState, hasExplicitConfirmation)
        auditService.logActionResult(result)
        return result
    }

    fun getRecentAuditEntries(): List<String> = auditService.loadRecentAuditEntries()
}
