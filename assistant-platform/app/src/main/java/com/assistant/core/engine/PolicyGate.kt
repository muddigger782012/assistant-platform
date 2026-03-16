package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import com.assistant.core.models.CapabilityState

class PolicyGate {

    data class PolicyResult(
        val allowed: Boolean,
        val reason: String
    )

    fun evaluate(request: ActionRequest, capabilityState: CapabilityState, confirmed: Boolean = false): PolicyResult {
        if (request.actionType == "REBOOT_DEVICE") {
            if (!capabilityState.dhizuku && !capabilityState.deviceOwner) {
                return PolicyResult(
                    allowed = false,
                    reason = "REBOOT_DEVICE requires Dhizuku or device owner capability"
                )
            }
        }

        if (request.riskLevel >= 2 && !confirmed) {
            return PolicyResult(
                allowed = false,
                reason = "Action '${request.actionType}' has risk level ${request.riskLevel} and requires explicit confirmation"
            )
        }

        return PolicyResult(allowed = true, reason = "Policy check passed")
    }
}
