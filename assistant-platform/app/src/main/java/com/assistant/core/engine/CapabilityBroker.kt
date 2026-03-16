package com.assistant.core.engine

import android.content.Context
import com.assistant.core.adapters.DhizukuAdapter
import com.assistant.core.adapters.ShizukuAdapter
import com.assistant.core.adapters.SpecialAccessAdapter
import com.assistant.core.adapters.StandardAdapter
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState

class CapabilityBroker(context: Context) {

    private val standardAdapter = StandardAdapter(context)
    private val specialAccessAdapter = SpecialAccessAdapter(context)
    private val shizukuAdapter = ShizukuAdapter(context)
    private val dhizukuAdapter = DhizukuAdapter(context)

    fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        val requiredCap = request.requiredCapability
        return when (requiredCap) {
            "STANDARD" -> {
                if (capabilityState.standard) standardAdapter.execute(request)
                else failResult(request.id, "Standard capability not available")
            }
            "SPECIAL_ACCESS" -> {
                if (capabilityState.specialAccess) specialAccessAdapter.execute(request)
                else failResult(request.id, "Special access not available")
            }
            "SHIZUKU" -> {
                if (capabilityState.shizuku) shizukuAdapter.execute(request)
                else failResult(request.id, "Shizuku not available")
            }
            "DHIZUKU" -> {
                if (capabilityState.dhizuku) dhizukuAdapter.execute(request)
                else failResult(request.id, "Dhizuku not available")
            }
            "DEVICE_OWNER" -> {
                if (capabilityState.deviceOwner) standardAdapter.execute(request)
                else failResult(request.id, "Device owner capability not available")
            }
            else -> failResult(request.id, "Unknown capability: $requiredCap")
        }
    }

    private fun failResult(id: String, message: String): ActionResult = ActionResult(
        id = id,
        success = false,
        adapterUsed = "NONE",
        message = message
    )
}
