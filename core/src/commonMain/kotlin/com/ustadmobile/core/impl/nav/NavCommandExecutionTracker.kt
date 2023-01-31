package com.ustadmobile.core.impl.nav

import com.ustadmobile.door.util.systemTimeInMillis

/**
 * NavCommands are emitted from ViewModels. A ViewModel might issue a navigation command before
 * the Fragment/React component is ready, which should be executed as soon as possible (e.g. the
 * Redirect, or when a user needs to login to see a screen, etc).
 *
 * A ViewModel on Android can remain active when the user leaves a fragment. We need to avoid the
 * possibility to "replay" navigation in this situation (e.g. when the user goes back).  We generally
 * want to avoid any accidental navigation that might come from a ViewModel that is not attached
 * to an active component.
 *
 * NavCommandExecutionTracker remembers the timestamp of a navigation command so that no command is
 * ever executed more than once. It also applies a timeout to avoid accidentally running a
 * nav command for a screen that is not active.
 *
 * It can be used as follows:
 *
 * navCommandFlow.collect { cmd ->
 *     navCommandExecutionTracker.runIfNotExecutedOrTimedOut(cmd) {
 *         //Do the actual navigation now.
 *     }
 * }
 *
 */
class NavCommandExecutionTracker(
    private val timeout: Long = DEFAULT_TIMEOUT,
) {

    private val executedTimestamps = mutableSetOf<Long>()

    fun runIfNotExecutedOrTimedOut(
        navCommand: NavCommand,
        block: (NavCommand) -> Unit
    ) {
        val timeNow = systemTimeInMillis()
        if(navCommand.timestamp !in executedTimestamps && (timeNow - navCommand.timestamp) < timeout) {
            block(navCommand)
            executedTimestamps += navCommand.timestamp
        }

        executedTimestamps.removeAll { (timeNow - it) > timeout }
    }

    companion object {

        const val DEFAULT_TIMEOUT = 5000L
    }

}