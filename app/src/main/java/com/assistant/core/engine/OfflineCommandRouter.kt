package com.assistant.core.engine

/**
 * Deterministic, offline-first command parser. No network, model or Android runtime required.
 * Unrecognized commands never silently execute a privileged action.
 */
object OfflineCommandRouter {
    enum class Action { CREATE_PROJECT, SHOW_STATUS, OPEN_APP, SET_VOLUME, SET_TIMER, DIAL_NUMBER, UNKNOWN }
    data class Command(val action: Action, val argument: String? = null, val requiresConfirmation: Boolean = false)

    fun parse(input: String): Command {
        val raw = input.trim()
        if (raw.isEmpty()) return Command(Action.UNKNOWN)
        val s = raw.lowercase()
        val project = Regex("^(?:create|make) (?:a )?project(?: named| called)? (.+)$", RegexOption.IGNORE_CASE).matchEntire(raw)
        if (project != null) {
            val name = project.groupValues[1].trim()
            return if (name.matches(Regex("[a-zA-Z0-9][a-zA-Z0-9_-]{0,63}")))
                Command(Action.CREATE_PROJECT, name) else Command(Action.UNKNOWN)
        }
        if (s in setOf("status", "show status", "capability status", "show capability status"))
            return Command(Action.SHOW_STATUS)
        val app = Regex("^(?:open|launch) (?:the )?app (.+)$", RegexOption.IGNORE_CASE).matchEntire(raw)
        if (app != null) return Command(Action.OPEN_APP, app.groupValues[1].trim())
        val volume = Regex("^(?:set )?volume(?: to)? (\\d{1,3})%?$", RegexOption.IGNORE_CASE).matchEntire(raw)
        if (volume != null) {
            val level = volume.groupValues[1].toInt()
            return if (level in 0..100) Command(Action.SET_VOLUME, level.toString()) else Command(Action.UNKNOWN)
        }
        val timer = Regex("^(?:set )?(?:a )?timer(?: for)? (\\d{1,4}) (seconds?|minutes?|hours?)$", RegexOption.IGNORE_CASE).matchEntire(raw)
        if (timer != null) {
            val count = timer.groupValues[1].toLong()
            val unit = timer.groupValues[2].lowercase()
            val multiplier = when { unit.startsWith("hour") -> 3600; unit.startsWith("minute") -> 60; else -> 1 }
            val seconds = count * multiplier
            return if (seconds in 1..86400) Command(Action.SET_TIMER, seconds.toString()) else Command(Action.UNKNOWN)
        }
        val dial = Regex("^(?:dial|call) ([+0-9() .-]{7,24})$", RegexOption.IGNORE_CASE).matchEntire(raw)
        if (dial != null) {
            val number = dial.groupValues[1].filter { it.isDigit() || it == '+' }
            return if (number.count { it.isDigit() } in 7..15 && number.count { it == '+' } <= 1 &&
                (!number.contains('+') || number.startsWith('+')))
                Command(Action.DIAL_NUMBER, number, requiresConfirmation = true)
                else Command(Action.UNKNOWN)
        }
        return Command(Action.UNKNOWN)
    }
}
