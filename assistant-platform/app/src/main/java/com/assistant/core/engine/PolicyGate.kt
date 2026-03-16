package com.assistant.core.engine

import com.assistant.core.models.ActionRequest
import com.assistant.core.models.CapabilityState

class PolicyGate {

    fun isAllowed(request: ActionRequest, capabilityState: CapabilityState, hasExplicitConfirmation: Boolean = false): Boolean {
        when (request.actionType) {
            IntentClassifier.REBOOT_DEVICE -> {
                return (capabilityState.dhizuku || capabilityState.deviceOwner) && hasExplicitConfirmation
            }
            else -> {
                when {
                    request.riskLevel <= 1 -> return true
                    request.riskLevel >= 2 -> return hasExplicitConfirmation
                    else -> return true
                }
            }
        }
    }
}
