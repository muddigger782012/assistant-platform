package com.assistant.core.engine

import com.assistant.core.adapters.DhizukuAdapter
import com.assistant.core.adapters.ShizukuAdapter
import com.assistant.core.adapters.SpecialAccessAdapter
import com.assistant.core.adapters.StandardAdapter
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.CapabilityState

class CapabilityBroker(
    private val standardAdapter: StandardAdapter,
    private val specialAccessAdapter: SpecialAccessAdapter,
    private val shizukuAdapter: ShizukuAdapter,
    private val dhizukuAdapter: DhizukuAdapter,
) {

    fun selectAdapter(request: ActionRequest, capabilityState: CapabilityState): ActionAdapter? {
        val adapter = when (request.requiredCapability) {
            ActionRegistry.CAPABILITY_STANDARD -> standardAdapter
            ActionRegistry.CAPABILITY_SPECIAL_ACCESS ->
                if (capabilityState.specialAccess) specialAccessAdapter else null

            ActionRegistry.CAPABILITY_SHIZUKU ->
                if (capabilityState.shizuku && shizukuAdapter.isAvailable()) shizukuAdapter else null

            ActionRegistry.CAPABILITY_DHIZUKU ->
                if (capabilityState.dhizuku || capabilityState.deviceOwner) dhizukuAdapter else null

            else -> null
        }

        if (adapter != null) {
            return adapter
        }

        if (request.allowFallback && standardAdapter.supports(request.actionType)) {
            return standardAdapter
        }

        return null
    }
}
