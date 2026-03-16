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

class CapabilityBroker(
    private val context: Context,
    private val capabilityState: CapabilityState
) {

    private val standardAdapter = StandardAdapter(context, capabilityState)
    private val shizukuAdapter = ShizukuAdapter()
    private val dhizukuAdapter = DhizukuAdapter()
    private val specialAccessAdapter = SpecialAccessAdapter()

    fun execute(request: ActionRequest): ActionResult {
        return when (request.requiredCapability.uppercase()) {
            "STANDARD" -> standardAdapter.execute(request)
            "SHIZUKU" -> {
                if (capabilityState.shizuku) {
                    shizukuAdapter.execute(request)
                } else if (request.allowFallback) {
                    ActionResult(
                        id = UUID.randomUUID().toString(),
                        success = false,
                        adapterUsed = ShizukuAdapter.ADAPTER_NAME,
                        message = "Shizuku not available. Cannot execute: ${request.actionType}"
                    )
                } else {
                    unavailableResult(request, "Shizuku")
                }
            }
            "DHIZUKU" -> {
                if (capabilityState.dhizuku || capabilityState.deviceOwner) {
                    dhizukuAdapter.execute(request)
                } else {
                    ActionResult(
                        id = UUID.randomUUID().toString(),
                        success = false,
                        adapterUsed = DhizukuAdapter.ADAPTER_NAME,
                        message = "Dhizuku/Device Owner not available. Cannot execute: ${request.actionType}"
                    )
                }
            }
            "SPECIAL_ACCESS" -> specialAccessAdapter.execute(request)
            else -> ActionResult(
                id = UUID.randomUUID().toString(),
                success = false,
                adapterUsed = "None",
                message = "Unknown capability: ${request.requiredCapability}"
            )
        }
    }

    private fun unavailableResult(request: ActionRequest, capability: String): ActionResult {
        return ActionResult(
            id = UUID.randomUUID().toString(),
            success = false,
            adapterUsed = "None",
            message = "$capability capability required for ${request.actionType} but is not available."
        )
    }
}
