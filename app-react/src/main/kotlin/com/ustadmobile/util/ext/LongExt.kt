package com.ustadmobile.util.ext

import com.ustadmobile.door.util.systemTimeInMillis
import kotlin.js.Date

/**
 * Convert milliseconds to date
 */
fun Long?.toDate(): Date {
    val mills = when {
        this ?: 0L == 0L -> systemTimeInMillis()
        this == Long.MAX_VALUE -> 8640000000000000
        else -> this
    }
    return Date(mills!!)
}

fun Long?.isSetDate(): Boolean {
    return this != null && this != 0L && this != 8640000000000000
}
