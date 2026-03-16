package com.assistant.core.engine

import android.content.Context
import com.assistant.core.models.CapabilityState
import rikka.shizuku.Shizuku

class CapabilityDetector(private val context: Context) {

    fun detect(): CapabilityState {
        val shizukuAvailable = try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }

        val deviceOwner = try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            dpm.isDeviceOwnerApp(context.packageName)
        } catch (e: Exception) {
            false
        }

        return CapabilityState(
            standard = true,
            specialAccess = false,
            shizuku = shizukuAvailable,
            dhizuku = false,
            deviceOwner = deviceOwner,
            oemPrivileged = false,
            platformSigned = false
        )
    }
}
