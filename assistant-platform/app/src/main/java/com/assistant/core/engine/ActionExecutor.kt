package com.assistant.core.engine

import android.content.Context
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState

class ActionExecutor(
    private val context: Context,
    private val capabilityState: CapabilityState
) {

    private val policyGate = PolicyGate()
    private val broker = CapabilityBroker(context, capabilityState)

    fun execute(request: ActionRequest): ActionResult {
        val decision = policyGate.evaluate(request, capabilityState)
        if (!decision.allowed) {
            return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = "PolicyGate",
                message = "Action blocked: ${decision.reason}"
            )
        }
        return broker.execute(request)
    }
}
