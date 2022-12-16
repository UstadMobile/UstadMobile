package com.ustadmobile.hooks

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.jsmodules.Intl
import com.ustadmobile.util.ext.toJsDateFromOtherTimeZoneToSystemTimeZone
import react.useMemo

/**
 * Format and display the date for a given time in millis using the display locale. This will try
 * to use Intl (available in most modern browsers), but will then fallback to using the standard
 * Javascript Date.toDateString
 *
 * @param timeInMillis time since epoch in ms
 * @param timezoneId the timezone to use to calculate the date
 */
fun useFormattedDate(timeInMillis: Long, timezoneId: String) : String{
    return useMemo(dependencies = arrayOf(timeInMillis, timezoneId)) {
        val dateOffsetForTimezone = timeInMillis.toJsDateFromOtherTimeZoneToSystemTimeZone(timezoneId)
        try {
            dateOffsetForTimezone?.let {
                Intl.Companion.DateTimeFormat(UstadMobileSystemImpl.displayedLocale).format(it)
            } ?: ""
        }catch (e: Exception) {
            dateOffsetForTimezone?.toDateString() ?: ""
        }
    }
}
