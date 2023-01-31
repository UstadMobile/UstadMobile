package com.ustadmobile.core.impl

import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.core.impl.nav.NavCommandExecutionTracker
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.door.util.systemTimeInMillis
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

class NavCommandExecutionTrackerTest {

    @Test
    fun givenNavCommand_whenRunOrExecuteCalled_thenShouldExecuteOnce() {
        val execTracker = NavCommandExecutionTracker()
        val navCommand = NavigateNavCommand("ViewName", mapOf(),
            UstadMobileSystemCommon.UstadGoOptions.Default)

        val counter = AtomicInteger()

        (1..2).forEach { _ ->
            execTracker.runIfNotExecutedOrTimedOut(navCommand) { counter.incrementAndGet() }
        }

        assertEquals(1, counter.get(), "Command was executed once")
    }

    @Test
    fun givenNavCommandTimedOut_whenRunOrExecuteCalled_thenShouldNotExecute() {
        val execTracker = NavCommandExecutionTracker()
        val navCommand = mock<NavCommand> {
            on { timestamp }.thenReturn((systemTimeInMillis() - NavCommandExecutionTracker.DEFAULT_TIMEOUT) -1)
        }

        val counter = AtomicInteger()

        execTracker.runIfNotExecutedOrTimedOut(navCommand) { counter.incrementAndGet() }

        assertEquals(0, counter.get(), "Timed out nav command was not executed")
    }

}