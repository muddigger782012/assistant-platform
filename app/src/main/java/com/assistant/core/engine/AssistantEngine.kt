package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import com.assistant.core.services.AuditService
import com.assistant.core.storage.ActionRepository
import java.util.UUID

class AssistantEngine(
    private val capabilityDetector: CapabilityDetector,
    private val intentClassifier: IntentClassifier,
    private val policyGate: PolicyGate,
    private val actionExecutor: ActionExecutor,
    private val actionRepository: ActionRepository,
    private val auditService: AuditService,
) {

    private var lastCapabilityState: CapabilityState = capabilityDetector.detect()

    fun refreshCapabilities(): CapabilityState {
        lastCapabilityState = capabilityDetector.detect()
        return lastCapabilityState
    }

    fun getCapabilityState(): CapabilityState = lastCapabilityState

    fun executeCommand(rawCommand: String, explicitConfirmation: Boolean = false): ActionResult {
        val request = intentClassifier.classify(rawCommand)
            ?: return ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "IntentClassifier",
                message = "Unsupported command: $rawCommand",
            ).also {
                auditService.logActionResult(
                    actionId = it.id,
                    adapterUsed = it.adapterUsed,
                    status = "failure",
                    message = it.message,
                )
            }
        return executeAction(request, explicitConfirmation)
    }

    fun executeAction(request: ActionRequest, explicitConfirmation: Boolean = false): ActionResult {
        lastCapabilityState = capabilityDetector.detect()
        actionRepository.insertAction(request)

        val decision = policyGate.evaluate(request, lastCapabilityState, explicitConfirmation)
        if (!decision.allowed) {
            return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = "PolicyGate",
                message = decision.message,
            ).also {
                auditService.logActionResult(
                    actionId = request.id,
                    adapterUsed = it.adapterUsed,
                    status = "blocked",
                    message = it.message,
                )
            }
        }

        return actionExecutor.execute(request, lastCapabilityState).also { result ->
            auditService.logActionResult(
                actionId = request.id,
                adapterUsed = result.adapterUsed,
                status = if (result.success) "success" else "failure",
                message = result.message,
            )
        }
    }
}
