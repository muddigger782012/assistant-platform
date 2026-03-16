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
    fun hasCapability(capability: String): Boolean = when (capability) {
        "STANDARD" -> standard
        "SPECIAL_ACCESS" -> specialAccess
        "SHIZUKU" -> shizuku
        "DHIZUKU" -> dhizuku
        "DEVICE_OWNER" -> deviceOwner
        "OEM_PRIVILEGED" -> oemPrivileged
        "PLATFORM_SIGNED" -> platformSigned
        else -> false
    }

    override fun toString(): String = buildString {
        append("standard=$standard, ")
        append("specialAccess=$specialAccess, ")
        append("shizuku=$shizuku, ")
        append("dhizuku=$dhizuku, ")
        append("deviceOwner=$deviceOwner, ")
        append("oemPrivileged=$oemPrivileged, ")
        append("platformSigned=$platformSigned")
    }
}
