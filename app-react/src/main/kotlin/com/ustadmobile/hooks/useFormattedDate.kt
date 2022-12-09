package com.ustadmobile.hooks

import react.useMemo
import kotlin.js.Date

/**
 *
 */
fun useFormattedDate(date: Long, timezoneId: String) : String{
    return useMemo(dependencies = arrayOf(date, timezoneId)) {
        Date(date).toDateString()
    }
}
