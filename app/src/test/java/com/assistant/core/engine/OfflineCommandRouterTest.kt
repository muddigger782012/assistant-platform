package com.assistant.core.engine

import org.junit.Assert.*
import org.junit.Test

class OfflineCommandRouterTest {
    @Test fun projectNamesAreSafe() {
        assertEquals("demo_1", OfflineCommandRouter.parse("create project named demo_1").argument)
        assertEquals(OfflineCommandRouter.Action.UNKNOWN, OfflineCommandRouter.parse("create project ../secret").action)
        assertEquals(OfflineCommandRouter.Action.UNKNOWN, OfflineCommandRouter.parse("create project a/b").action)
    }
    @Test fun parsesLocalCommands() {
        assertEquals(OfflineCommandRouter.Action.SHOW_STATUS, OfflineCommandRouter.parse("show status").action)
        assertEquals("75", OfflineCommandRouter.parse("set volume to 75%").argument)
        assertEquals("300", OfflineCommandRouter.parse("set a timer for 5 minutes").argument)
        assertEquals(OfflineCommandRouter.Action.OPEN_APP, OfflineCommandRouter.parse("open app Maps").action)
    }
    @Test fun rejectsUnsafeInputs() {
        assertEquals(OfflineCommandRouter.Action.UNKNOWN, OfflineCommandRouter.parse("volume 101").action)
        assertEquals(OfflineCommandRouter.Action.UNKNOWN, OfflineCommandRouter.parse("timer for 0 seconds").action)
        assertEquals(OfflineCommandRouter.Action.UNKNOWN, OfflineCommandRouter.parse("timer for 9999 hours").action)
        assertEquals(OfflineCommandRouter.Action.UNKNOWN, OfflineCommandRouter.parse("reboot").action)
    }
    @Test fun dialNeedsConfirmation() {
        val command = OfflineCommandRouter.parse("call (757) 555-0123")
        assertEquals(OfflineCommandRouter.Action.DIAL_NUMBER, command.action)
        assertEquals("7575550123", command.argument)
        assertTrue(command.requiresConfirmation)
    }
}
