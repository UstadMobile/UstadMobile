package com.ustadmobile.util.ext

import kotlin.js.Date

/**
 * Convert milliseconds to date
 */
fun Long?.toDate(): Date {
    return Date(if(this != null && this != 0L) this else Date.now().toLong())
}
