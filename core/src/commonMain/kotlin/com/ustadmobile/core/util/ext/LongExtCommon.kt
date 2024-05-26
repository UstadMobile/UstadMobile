package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.MS_PER_HOUR

/**
 * Unix timestamp for maximum date we will recognize as being a set date: 1/Jan/2200
 */
const val MAX_VALID_DATE = 7258118400000

/**
 * Unix timestamp 24hours beyond the maximum date that we will recognize. We must not use Long.MAX_VALUE,
 * because adding anything (e.g. timezone offset) to MAX_VALUE will wrap to the most negative possible
 * value, leading to unpredictable behavior.
 */
const val UNSET_DISTANT_FUTURE = MAX_VALID_DATE + (24 * MS_PER_HOUR)


/**
 * All date fields in Ustad are stored as 64 bit longs in ms since epoch. When a start date is unset
 * it is given a default value of zero. When an end date is unset, it is given a default value of
 * UNSET_DISTANT_FUTURE.
 *
 * This function will determine if this Long has been set (e.g. probably should be displayed), or is
 * just a default value.
 *
 * @receiver a nullable Long representing a timestamp
 * @return true if this is a non null value set to something other than 0 or Long.MAX_VALUE, false otherwise
 */
fun Long?.isDateSet(): Boolean {
    return this != null && this > 0 && this < MAX_VALID_DATE
}


/**
 * Converts a Long to a byte array without requiring ByteBuffer
 */
fun Long.toByteArray(): ByteArray {
    val buffer = ByteArray(8)
    var temp = this
    for (i in 0 until 8) {
        buffer[7 - i] = (temp and 0xFF).toByte()
        temp = temp shr 8
    }
    return buffer
}