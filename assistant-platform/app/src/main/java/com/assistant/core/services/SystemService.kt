package com.assistant.core.services

import com.assistant.core.models.CapabilityState

class SystemService(private val appFilesDir: String) {

    fun getStatusSummary(capabilities: CapabilityState): String {
        return buildString {
            appendLine("=== Assistant Platform Status ===")
            appendLine("App files dir: $appFilesDir")
            appendLine("--- Capabilities ---")
            appendLine("Standard:      ${capabilities.standard}")
            appendLine("Special Access:${capabilities.specialAccess}")
            appendLine("Shizuku:       ${capabilities.shizuku}")
            appendLine("Dhizuku:       ${capabilities.dhizuku}")
            appendLine("Device Owner:  ${capabilities.deviceOwner}")
            appendLine("OEM Privileged:${capabilities.oemPrivileged}")
            appendLine("Platform Signed:${capabilities.platformSigned}")
        }
    }
}
