package com.ustadmobile.core.util.ext

import org.quartz.Scheduler

private const val RETRY_WAIT_KEY = "com.ustadmobile.wait"

private const val DEFAULT_RETRY_WAIT = 10_000L

/**
 * Set the default retryWait, will be used by any jobs that retry.
 */
var Scheduler.retryWait: Long
    set(value) {
        context.put(RETRY_WAIT_KEY, value)
    }

    get() = if(context.containsKey(RETRY_WAIT_KEY)) {
        context.getLong(RETRY_WAIT_KEY)
    }else {
        DEFAULT_RETRY_WAIT
    }
