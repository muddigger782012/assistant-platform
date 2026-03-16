package com.assistant.core.engine

import android.content.Context
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState

class ActionExecutor(context: Context) {

    private val capabilityBroker = CapabilityBroker(context)

    fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        return capabilityBroker.execute(request, capabilityState)
    }
}
