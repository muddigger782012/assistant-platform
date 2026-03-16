package com.assistant.core.adapters

import com.assistant.core.engine.ActionAdapter
import com.assistant.core.engine.ActionRegistry
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState

class DhizukuAdapter : ActionAdapter {

    override val adapterName: String = "DhizukuAdapter"

    fun isAvailable(capabilityState: CapabilityState): Boolean = capabilityState.dhizuku || capabilityState.deviceOwner

    override fun supports(actionType: String): Boolean = actionType == ActionRegistry.ACTION_REBOOT_DEVICE

    override fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        if (!isAvailable(capabilityState)) {
            return ActionResult(
                id = request.id,
                success = false,
                adapterUsed = adapterName,
                message = "Dhizuku is not available. Reboot is unsupported in this MVP.",
            )
        }

        return ActionResult(
            id = request.id,
            success = false,
            adapterUsed = adapterName,
            message = "Reboot is intentionally left as a safe placeholder for Dhizuku or device-owner mode.",
        )
    }
}
