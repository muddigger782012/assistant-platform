package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState

interface ActionAdapter {
    val adapterName: String
    fun supports(actionType: String): Boolean
    fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult
}

class ActionExecutor(private val capabilityBroker: CapabilityBroker) {

    fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        val adapter = capabilityBroker.selectAdapter(request, capabilityState)
            ?: return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = "CapabilityBroker",
                message = "No adapter is available for ${request.requiredCapability}.",
            )

        if (!adapter.supports(request.actionType)) {
            return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = adapter.adapterName,
                message = "Adapter ${adapter.adapterName} does not support ${request.actionType}.",
            )
        }

        return adapter.execute(request, capabilityState)
    }
}
