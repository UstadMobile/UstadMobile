package com.ustadmobile.hooks

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.wrappers.intl.Intl
import com.ustadmobile.wrappers.intl.IntlDateTimeNumericProp
import com.ustadmobile.util.ext.setHours
import js.core.jso
import react.useMemo
import kotlin.js.Date

/**
 * Wrapper for useMemo that will format a time according to the display locale.
 *
 * @param timeInMillisSinceMidnight The time of day in milliseconds since midnight
 */
fun useFormattedTime(timeInMillisSinceMidnight: Int): String {
    return useMemo(dependencies = arrayOf(timeInMillisSinceMidnight)) {
        val date = Date()
        date.setHours(timeInMillisSinceMidnight / MS_PER_HOUR,
            timeInMillisSinceMidnight.mod(MS_PER_HOUR) / MS_PER_MIN,
            timeInMillisSinceMidnight.mod(MS_PER_MIN) / 1000,
        )

        Intl.Companion.DateTimeFormat(UstadMobileSystemImpl.displayedLocale, jso {
            hour = IntlDateTimeNumericProp.numeric
            minute = IntlDateTimeNumericProp.numeric
        }).format(date)
    }
}