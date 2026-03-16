package com.assistant.core.engine

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.assistant.core.models.CapabilityState
import rikka.shizuku.Shizuku

class CapabilityDetector {

    fun detect(context: Context): CapabilityState {
        return CapabilityState(
            standard = true,
            specialAccess = false,
            shizuku = detectShizuku(),
            dhizuku = false,
            deviceOwner = isDeviceOwner(context),
            oemPrivileged = false,
            platformSigned = false
        )
    }

    private fun detectShizuku(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    private fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        return dpm?.isDeviceOwnerApp(context.packageName) == true
    }
}
