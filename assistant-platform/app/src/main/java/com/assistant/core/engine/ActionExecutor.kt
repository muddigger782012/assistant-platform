package com.assistant.core.engine

import android.content.Context
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState

class ActionExecutor(context: Context) {

    private val capabilityBroker = CapabilityBroker(context)
    private val policyGate = PolicyGate()

    fun execute(request: ActionRequest, capabilityState: CapabilityState, hasExplicitConfirmation: Boolean = true): ActionResult {
        if (!policyGate.isAllowed(request, capabilityState, hasExplicitConfirmation)) {
            return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = "PolicyGate",
                message = "Action blocked by policy"
            )
        }
        return capabilityBroker.execute(request, capabilityState)
    }
}
