package com.assistant.core.engine

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.assistant.core.models.CapabilityState
import rikka.shizuku.Shizuku

class CapabilityDetector(private val context: Context) {

    fun detect(): CapabilityState {
        return CapabilityState(
            standard = true,
            specialAccess = false,
            shizuku = detectShizuku(),
            dhizuku = false,
            deviceOwner = detectDeviceOwner(),
            oemPrivileged = false,
            platformSigned = false
        )
    }

    private fun detectShizuku(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    private fun detectDeviceOwner(): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val component = ComponentName(context, context.javaClass)
            dpm.isDeviceOwnerApp(context.packageName)
        } catch (e: Exception) {
            false
        }
    }
}
