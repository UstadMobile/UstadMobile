package com.ustadmobile.core.util.ext

/**
 * All date fields in Ustad are stored as 64 bit longs in ms since epoch. When a start date is unset
 * it is given a default value of zero. When an end date is unset, it is given a default value of
 * Long.MAX_VALUE.
 *
 * This function will determine if this Long has been set (e.g. probably should be displayed), or is
 * just a default value.
 *
 * @receiver a nullable Long representing a timestamp
 * @return true if this is a non null value set to something other than 0 or Long.MAX_VALUE, false otherwise
 */
fun Long?.isDateSet(): Boolean {
    return this != null && this > 0 && this < Long.MAX_VALUE
}

