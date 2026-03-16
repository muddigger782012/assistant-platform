package com.assistant.core.services

import android.content.Context
import com.assistant.core.models.CapabilityState

class SystemService(private val context: Context) {

    fun getStatusSummary(capabilityState: CapabilityState): String {
        return buildString {
            appendLine("=== Assistant Platform Status ===")
            appendLine("App files dir: ${context.filesDir.absolutePath}")
            appendLine("Projects dir:  ${context.filesDir.absolutePath}/projects")
            appendLine()
            append(capabilityState.toString())
            appendLine("Device owner active: ${capabilityState.deviceOwner}")
        }
    }
}
