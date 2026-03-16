package com.assistant.core.engine

import android.content.Context
import com.assistant.core.adapters.DhizukuAdapter
import com.assistant.core.adapters.ShizukuAdapter
import com.assistant.core.adapters.SpecialAccessAdapter
import com.assistant.core.adapters.StandardAdapter
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState
import java.util.UUID

class CapabilityBroker(context: Context) {

    private val standardAdapter = StandardAdapter(context)
    private val shizukuAdapter = ShizukuAdapter()
    private val dhizukuAdapter = DhizukuAdapter()
    private val specialAccessAdapter = SpecialAccessAdapter()

    fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        return when (request.requiredCapability) {
            "STANDARD" -> standardAdapter.execute(request, capabilityState)
            "SHIZUKU" -> {
                if (shizukuAdapter.isAvailable()) {
                    shizukuAdapter.execute(request)
                } else if (request.allowFallback) {
                    ActionResult(
                        id = UUID.randomUUID().toString(),
                        success = false,
                        adapterUsed = "shizuku",
                        message = "Shizuku is not available. Install and start Shizuku to use this feature."
                    )
                } else {
                    ActionResult(
                        id = UUID.randomUUID().toString(),
                        success = false,
                        adapterUsed = "shizuku",
                        message = "Shizuku required but not available, and fallback is disabled."
                    )
                }
            }
            "DHIZUKU" -> {
                if (dhizukuAdapter.isAvailable()) {
                    dhizukuAdapter.execute(request)
                } else {
                    ActionResult(
                        id = UUID.randomUUID().toString(),
                        success = false,
                        adapterUsed = "dhizuku",
                        message = "Dhizuku is not available. Device owner delegation not configured."
                    )
                }
            }
            "SPECIAL_ACCESS" -> {
                if (specialAccessAdapter.isAvailable()) {
                    specialAccessAdapter.execute(request)
                } else {
                    ActionResult(
                        id = UUID.randomUUID().toString(),
                        success = false,
                        adapterUsed = "special_access",
                        message = "Special access not available."
                    )
                }
            }
            else -> ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "none",
                message = "Unknown capability requirement: ${request.requiredCapability}"
            )
        }
    }
}
