package com.assistant.core.engine

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.assistant.core.models.CapabilityState
import rikka.shizuku.Shizuku

class CapabilityDetector(private val context: Context) {

    fun detect(): CapabilityState {
        val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val isDeviceOwner = devicePolicyManager?.isDeviceOwnerApp(context.packageName) ?: false
        val shizukuAvailable = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }

        return CapabilityState(
            standard = true,
            specialAccess = false,
            shizuku = shizukuAvailable,
            dhizuku = false,
            deviceOwner = isDeviceOwner,
            oemPrivileged = false,
            platformSigned = false,
        )
    }
}
