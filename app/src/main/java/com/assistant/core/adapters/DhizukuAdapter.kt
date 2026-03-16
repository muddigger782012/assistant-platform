package com.assistant.core.adapters

import com.assistant.core.engine.ActionRegistry
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState

class DhizukuAdapter {

    fun isAvailable(capabilityState: CapabilityState): Boolean = capabilityState.dhizuku

    fun execute(actionRequest: ActionRequest, capabilityState: CapabilityState): ActionResult {
        if (!isAvailable(capabilityState)) {
            return ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "DHIZUKU",
                message = "Dhizuku unavailable. Device-admin operations are unsupported in MVP."
            )
        }

        return when (actionRequest.actionType) {
            ActionRegistry.REBOOT_DEVICE -> ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "DHIZUKU",
                message = "Reboot placeholder only. Full Dhizuku integration is not implemented."
            )
            else -> ActionResult(
                id = actionRequest.id,
                success = false,
                adapterUsed = "DHIZUKU",
                message = "Unsupported Dhizuku action: ${actionRequest.actionType}"
            )
        }
    }
}
