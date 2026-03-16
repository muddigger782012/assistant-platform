package com.assistant.core.engine

import android.content.Context
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import com.assistant.core.services.AuditService
import com.assistant.core.storage.ActionRepository
import java.util.UUID

class AssistantEngine(
    context: Context,
    private val auditService: AuditService,
    private val actionRepository: ActionRepository
) {

    private val intentClassifier = IntentClassifier()
    private val policyGate = PolicyGate()
    private val actionExecutor = ActionExecutor(context)
    private val actionRegistry = ActionRegistry()
    private val capabilityDetector = CapabilityDetector(context)

    var capabilityState: CapabilityState = capabilityDetector.detect()
        private set

    fun refreshCapabilities() {
        capabilityState = capabilityDetector.detect()
    }

    fun processCommand(input: String, confirmed: Boolean = false): ActionResult {
        val request = intentClassifier.classify(input)
        return executeAction(request, confirmed)
    }

    fun executeAction(request: ActionRequest, confirmed: Boolean = false): ActionResult {
        if (request.actionType == "UNKNOWN") {
            val result = ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "none",
                message = "Unrecognized command. Try: 'create project', 'run shell', 'show status', 'reboot'"
            )
            auditService.logResult(request.id, result)
            return result
        }

        actionRepository.insertAction(request)

        val policy = policyGate.evaluate(request, capabilityState, confirmed)
        if (!policy.allowed) {
            val result = ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "policy_gate",
                message = "Blocked: ${policy.reason}"
            )
            auditService.logResult(request.id, result)
            return result
        }

        val result = actionExecutor.execute(request, capabilityState)
        auditService.logResult(request.id, result)
        return result
    }
}
