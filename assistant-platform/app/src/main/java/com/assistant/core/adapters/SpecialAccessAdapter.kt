package com.assistant.core.adapters

import android.content.Context
import com.assistant.core.models.ActionRequest
import com.assistant.core.models.ActionResult

class SpecialAccessAdapter(private val context: Context) {

    fun execute(request: ActionRequest): ActionResult {
        return ActionResult(
            id = request.id,
            success = false,
            adapterUsed = "SpecialAccessAdapter",
            message = "Special access adapter is a placeholder for future accessibility/notification/usage access paths"
        )
    }
}
