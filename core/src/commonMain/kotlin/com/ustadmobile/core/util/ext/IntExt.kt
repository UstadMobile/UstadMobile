package com.ustadmobile.core.util.ext

fun Int.alternative(alternative: Int) = if(this == 0) alternative else this


fun Long.alternative(alternative: Long) = if(this == 0L) alternative else this

/**
 * Convenience extension function. Where the receiver is being used to hold
 * bitmask values, check if the given flag is set
 *
 * @param a bitmask flag
 * @receiver Integer being used to store bitmask values
 *
 * @return true if the flag is set, false otherwise
 */
fun Int.hasFlag(flag: Int) =  (this and flag) == flag
