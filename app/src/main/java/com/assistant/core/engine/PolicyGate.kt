package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import com.assistant.core.models.CapabilityState

data class PolicyDecision(
    val allowed: Boolean,
    val message: String,
)

class PolicyGate {

    fun evaluate(
        request: ActionRequest,
        capabilityState: CapabilityState,
        explicitConfirmation: Boolean = false,
    ): PolicyDecision {
        if (request.actionType == ActionRegistry.ACTION_REBOOT_DEVICE &&
            !capabilityState.dhizuku &&
            !capabilityState.deviceOwner
        ) {
            return PolicyDecision(
                allowed = false,
                message = "Reboot is blocked unless Dhizuku or device owner capability is available.",
            )
        }

        if (request.riskLevel >= 2 && !explicitConfirmation) {
            return PolicyDecision(
                allowed = false,
                message = "Action ${request.actionType} requires explicit confirmation.",
            )
        }

        return PolicyDecision(
            allowed = true,
            message = "Allowed",
        )
    }
}
