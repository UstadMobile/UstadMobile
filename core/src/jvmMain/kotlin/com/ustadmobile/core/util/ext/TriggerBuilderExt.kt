package com.ustadmobile.core.util.ext

import com.ustadmobile.door.util.systemTimeInMillis
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import java.util.*

/**
 * If the given time is the same or earlier than the current system time, then call startNow.
 * Otherwise call startAtTime
 */
fun <T: Trigger> TriggerBuilder<T>.startNowOrAt(time: Long) : TriggerBuilder<T> {
    if(time <= systemTimeInMillis()){
        startNow()
    }else {
        startAt(Date(time))
    }

    return this
}
