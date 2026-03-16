package com.assistant.core.services

import com.assistant.core.engine.ActionRegistry
import com.assistant.core.models.ActionRequest

data class ParsedVoiceCommand(
    val actionRequest: ActionRequest? = null,
    val fallbackTextCommand: String? = null,
    val localCommand: LocalVoiceCommand = LocalVoiceCommand.NONE,
    val responseHint: String? = null
)

enum class LocalVoiceCommand {
    NONE,
    STOP_VOICE,
    START_VOICE,
    OPEN_SETTINGS
}

class VoiceCommandParser(private val actionRegistry: ActionRegistry) {

    fun parse(raw: String): ParsedVoiceCommand {
        val normalized = normalize(raw)
        if (normalized.isBlank()) {
            return ParsedVoiceCommand(responseHint = "I did not catch that command.")
        }

        if (containsAny(normalized, "stop listening", "stop voice", "go silent", "disable voice")) {
            return ParsedVoiceCommand(localCommand = LocalVoiceCommand.STOP_VOICE, responseHint = "Stopping voice mode.")
        }
        if (containsAny(normalized, "start listening", "enable voice", "wake up")) {
            return ParsedVoiceCommand(localCommand = LocalVoiceCommand.START_VOICE, responseHint = "Voice mode enabled.")
        }
        if (containsAny(normalized, "open voice settings", "voice settings", "configure voice")) {
            return ParsedVoiceCommand(localCommand = LocalVoiceCommand.OPEN_SETTINGS, responseHint = "Opening voice settings.")
        }

        if (containsAny(normalized, "status", "capability", "what can you do", "show system status")) {
            return ParsedVoiceCommand(
                actionRequest = actionRegistry.showStatusRequest(),
                responseHint = "Showing current capability status."
            )
        }

        parseProjectCommand(normalized)?.let { return it }
        parseShellCommand(normalized)?.let { return it }
        parseRebootCommand(normalized)?.let { return it }

        return ParsedVoiceCommand(
            fallbackTextCommand = normalized,
            responseHint = "Processing command with fallback intent classifier."
        )
    }

    private fun parseProjectCommand(normalized: String): ParsedVoiceCommand? {
        val createTokens = listOf("create project", "new project", "generate project", "make project")
        if (createTokens.none { normalized.contains(it) }) return null

        val name = extractProjectName(normalized)
        return ParsedVoiceCommand(
            actionRequest = actionRegistry.createProjectRequest(name),
            responseHint = "Creating project $name."
        )
    }

    private fun parseShellCommand(normalized: String): ParsedVoiceCommand? {
        val shellPattern = Regex("(run|execute) (shell|command) (.+)")
        val match = shellPattern.find(normalized) ?: return null
        val command = match.groupValues.getOrNull(3)?.trim().orEmpty().ifBlank { "id" }
        return ParsedVoiceCommand(
            actionRequest = actionRegistry.runShellRequest(command = command, confirmed = true),
            responseHint = "Running shell command."
        )
    }

    private fun parseRebootCommand(normalized: String): ParsedVoiceCommand? {
        val wantsReboot = containsAny(normalized, "reboot", "restart device", "restart phone")
        if (!wantsReboot) return null

        val confirmed = containsAny(normalized, "confirm", "yes reboot", "reboot now")
        return ParsedVoiceCommand(
            actionRequest = actionRegistry.rebootDeviceRequest(confirmed = confirmed),
            responseHint = if (confirmed) "Attempting reboot action." else "Reboot request heard. Confirmation required."
        )
    }

    private fun extractProjectName(normalized: String): String {
        val pattern = Regex("(called|named)\\s+([a-z0-9_\\-]+)")
        val candidate = pattern.find(normalized)?.groupValues?.getOrNull(2)
        if (!candidate.isNullOrBlank()) return candidate
        return "assistant_demo"
    }

    private fun containsAny(text: String, vararg options: String): Boolean {
        return options.any { text.contains(it) }
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9_\\-\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
