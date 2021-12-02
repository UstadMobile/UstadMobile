package com.ustadmobile.util.ext

import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlin.js.Date

/**
 * Convert milliseconds to date
 */
fun Long?.toDate(): Date {
    return Date(if(this != null && this != 0L) this else getSystemTimeInMillis())
}
