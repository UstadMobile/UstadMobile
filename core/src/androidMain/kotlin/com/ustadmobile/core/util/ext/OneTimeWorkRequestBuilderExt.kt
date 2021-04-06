package com.ustadmobile.core.util.ext

import androidx.work.OneTimeWorkRequest
import com.ustadmobile.door.util.systemTimeInMillis
import java.util.concurrent.TimeUnit

/**
 * Set an initial delay on this WorkRequest if the desired runTime is in the future. Otherwise
 * do nothing (so the workrequest would run immediately)
 */
fun OneTimeWorkRequest.Builder.setInitialDelayIfLater(runTime: Long): OneTimeWorkRequest.Builder {
    val timeUntilRun = runTime - systemTimeInMillis()
    if(timeUntilRun > 0)
        setInitialDelay(runTime - systemTimeInMillis(), TimeUnit.MILLISECONDS)

    return this
}