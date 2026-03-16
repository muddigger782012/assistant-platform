package com.assistant.core.services

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.assistant.core.models.CapabilityState

class SystemService(private val context: Context) {

    fun buildStatusSummary(capabilityState: CapabilityState): String {
        val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val deviceOwnerActive = devicePolicyManager?.isDeviceOwnerApp(context.packageName) ?: false
        return buildString {
            appendLine("Assistant Platform status")
            appendLine("Files dir: ${context.filesDir.absolutePath}")
            appendLine("Device owner active: $deviceOwnerActive")
            appendLine("Capabilities:")
            appendLine("- standard: ${capabilityState.standard}")
            appendLine("- specialAccess: ${capabilityState.specialAccess}")
            appendLine("- shizuku: ${capabilityState.shizuku}")
            appendLine("- dhizuku: ${capabilityState.dhizuku}")
            appendLine("- deviceOwner: ${capabilityState.deviceOwner}")
            appendLine("- oemPrivileged: ${capabilityState.oemPrivileged}")
            appendLine("- platformSigned: ${capabilityState.platformSigned}")
        }.trim()
    }
}
