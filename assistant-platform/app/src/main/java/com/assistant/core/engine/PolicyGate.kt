package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import com.assistant.core.models.CapabilityState

class PolicyGate {

    data class PolicyDecision(val allowed: Boolean, val reason: String)

    fun evaluate(request: ActionRequest, capabilities: CapabilityState): PolicyDecision {
        // REBOOT_DEVICE requires dhizuku or deviceOwner
        if (request.actionType == "REBOOT_DEVICE") {
            if (!capabilities.dhizuku && !capabilities.deviceOwner) {
                return PolicyDecision(
                    allowed = false,
                    reason = "REBOOT_DEVICE requires Dhizuku or Device Owner capability, which is not available."
                )
            }
        }

        // Risk level 0-1: allow
        if (request.riskLevel <= 1) {
            return PolicyDecision(allowed = true, reason = "Low risk action permitted.")
        }

        // Risk level 2+: allow only if allowFallback is set (acts as explicit confirmation)
        if (request.riskLevel >= 2 && request.allowFallback) {
            return PolicyDecision(allowed = true, reason = "Elevated risk action permitted with fallback flag.")
        }

        return PolicyDecision(
            allowed = false,
            reason = "Action risk level ${request.riskLevel} requires explicit confirmation."
        )
    }
}
