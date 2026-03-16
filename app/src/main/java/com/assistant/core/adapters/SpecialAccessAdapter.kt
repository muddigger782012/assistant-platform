package com.assistant.core.adapters

import com.assistant.core.engine.ActionAdapter
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import com.assistant.core.models.CapabilityState

class SpecialAccessAdapter : ActionAdapter {

    override val adapterName: String = "SpecialAccessAdapter"

    override fun supports(actionType: String): Boolean = false

    override fun execute(request: ActionRequest, capabilityState: CapabilityState): ActionResult {
        return ActionResult(
            id = request.id,
            success = false,
            adapterUsed = adapterName,
            message = "Special access paths are placeholders in this MVP.",
        )
    }
}
