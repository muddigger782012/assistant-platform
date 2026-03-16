package com.assistant.core.adapters

import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import java.util.UUID

class SpecialAccessAdapter {

    fun isAvailable(): Boolean {
        return false
    }

    fun execute(request: ActionRequest): ActionResult {
        return ActionResult(
            id = UUID.randomUUID().toString(),
            success = false,
            adapterUsed = "special_access",
            message = "Special access adapter not yet implemented. Action: ${request.actionType}"
        )
    }
}
