package com.assistant.core.models

data class CapabilityState(
    val standard: Boolean = true,
    val specialAccess: Boolean = false,
    val shizuku: Boolean = false,
    val dhizuku: Boolean = false,
    val deviceOwner: Boolean = false,
    val oemPrivileged: Boolean = false,
    val platformSigned: Boolean = false
) {
    override fun toString(): String {
        return buildString {
            appendLine("Capability State:")
            appendLine("  standard       = $standard")
            appendLine("  specialAccess  = $specialAccess")
            appendLine("  shizuku        = $shizuku")
            appendLine("  dhizuku        = $dhizuku")
            appendLine("  deviceOwner    = $deviceOwner")
            appendLine("  oemPrivileged  = $oemPrivileged")
            appendLine("  platformSigned = $platformSigned")
        }
    }
}
