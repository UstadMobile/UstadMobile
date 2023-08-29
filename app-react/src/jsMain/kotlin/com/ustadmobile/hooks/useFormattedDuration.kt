package com.ustadmobile.hooks

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import react.useMemo

/**
 * Create a human readable string for a duration. E.g. "1 hour 49 minutes". For the moment this
 * does not really handle plurals.
 *
 * @param timeInMillis the duration in milliseconds
 * @return formatted string e.g. 1 hour 49 minutes
 */
fun useFormattedDuration(timeInMillis: Long): String {
    val stringsXml = useStringProvider()

    return useMemo(dependencies = arrayOf(timeInMillis)) {
        val hours = (timeInMillis / MS_PER_HOUR)
        val mins = timeInMillis.mod(MS_PER_HOUR) / MS_PER_MIN

        var str = ""
        if(hours > 0)
            str += "$hours ${stringsXml[MR.strings.xapi_hours]} "

        if(mins > 0)
            str += "$mins ${stringsXml[MR.strings.xapi_minutes]}"

        str
    }
}
