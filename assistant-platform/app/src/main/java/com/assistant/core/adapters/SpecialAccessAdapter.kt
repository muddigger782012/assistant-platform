package com.assistant.core.adapters

import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult
import java.util.UUID

class SpecialAccessAdapter {

    companion object {
        const val ADAPTER_NAME = "SpecialAccessAdapter"
    }

    fun isAvailable(): Boolean = false

    fun execute(request: ActionRequest): ActionResult {
        return ActionResult(
            id = UUID.randomUUID().toString(),
            success = false,
            adapterUsed = ADAPTER_NAME,
            message = "Special access adapter is a placeholder for future accessibility/notification/usage-access paths."
        )
    }
}
