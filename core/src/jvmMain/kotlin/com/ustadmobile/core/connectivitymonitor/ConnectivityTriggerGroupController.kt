package com.ustadmobile.core.connectivitymonitor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.quartz.Scheduler
import org.quartz.impl.matchers.GroupMatcher

/**
 * Manage triggering Quartz Jobs which require connectivity. This is similar to using WorkManager on
 * Android's connectivity constraint.
 *
 * This works by observing the flow from the connectivity monitor to resume and pause a trigger group.
 *
 * A job that requires connectivity therefor should use a triggerkey with the
 */
class ConnectivityTriggerGroupController(
    private val scheduler: Scheduler,
    private val connectivityMonitorJvm: ConnectivityMonitorJvm,
) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    init {
        scope.launch {
            connectivityMonitorJvm.state.collect {
                when(it) {
                    ConnectivityMonitorJvm.ConnectivityStatus.CONNECTED -> {
                        scheduler.resumeTriggers(GroupMatcher.groupEquals(
                            TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP))
                    }
                    ConnectivityMonitorJvm.ConnectivityStatus.DISCONNECTED -> {
                        scheduler.pauseTriggers(GroupMatcher.groupEquals(
                            TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP))
                    }
                    else -> {
                        //do nothing
                    }
                }
            }
        }
    }

    fun cancel() {
        scope.cancel()
    }

    companion object {

        /**
         * This triggerkey group will be running whilst connectivity is available, and paused when
         * offline. See class doc comment.
         */
        const val TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP = "com.ustadmobile.pendingconnection"

    }
}