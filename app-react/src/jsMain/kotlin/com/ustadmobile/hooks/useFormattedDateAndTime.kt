package com.ustadmobile.hooks

import com.ustadmobile.mui.components.UstadLanguageConfigContext
import com.ustadmobile.util.ext.toJsDateFromOtherTimeZoneToSystemTimeZone
import com.ustadmobile.wrappers.intl.Intl
import com.ustadmobile.wrappers.intl.IntlDateTimeStyleProp
import js.objects.jso
import react.useContext
import react.useMemo

/**
 * Wrapper for useMemo to format a date / time string
 *
 * @param timeInMillis time since epoch in ms
 * @param timezoneId the timezone to use to calculate the date
 */
fun useFormattedDateAndTime(timeInMillis: Long, timezoneId: String): String {
    val langConfig = useContext(UstadLanguageConfigContext)

    return useMemo(dependencies = arrayOf(timeInMillis, timezoneId)) {
        val dateOffsetForTimezone = timeInMillis.toJsDateFromOtherTimeZoneToSystemTimeZone(timezoneId)
        try {
            dateOffsetForTimezone?.let {
                Intl.Companion.DateTimeFormat(langConfig?.displayedLocale ?: "en", jso {
                    timeStyle = IntlDateTimeStyleProp.medium
                    dateStyle = IntlDateTimeStyleProp.medium
                }).format(it)
            } ?: ""
        }catch(e: Exception) {
            dateOffsetForTimezone?.toDateString() + " " + dateOffsetForTimezone?.toTimeString()
        }
    }
}
