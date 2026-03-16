package com.assistant.core.engine

object IntentClassifier {

    const val CREATE_PROJECT = "CREATE_PROJECT"
    const val RUN_SHELL = "RUN_SHELL"
    const val REBOOT_DEVICE = "REBOOT_DEVICE"
    const val SHOW_STATUS = "SHOW_STATUS"
    const val UNKNOWN = "UNKNOWN"

    fun classify(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("create project") -> CREATE_PROJECT
            lower.contains("run shell") -> RUN_SHELL
            lower.contains("reboot") -> REBOOT_DEVICE
            lower.contains("show status") || lower.contains("status") -> SHOW_STATUS
            else -> UNKNOWN
        }
    }
}
