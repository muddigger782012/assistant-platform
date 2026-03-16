package com.assistant.core.services

import android.content.Context
import com.assistant.core.models.CapabilityState

class SystemService(private val context: Context) {

    fun produceStatusSummary(capabilityState: CapabilityState): String {
        val filesDir = context.filesDir?.absolutePath ?: "N/A"
        val deviceOwner = capabilityState.deviceOwner
        return buildString {
            appendLine("=== System Status ===")
            appendLine("App files dir: $filesDir")
            appendLine("Capability state: $capabilityState")
            appendLine("Device owner active: $deviceOwner")
        }
    }
}
