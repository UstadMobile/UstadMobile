package com.ustadmobile.core.util.ext

/**
 * Convenience extension function. Where the receiver is being used to hold
 * bitmask values, check if the given flag is set
 *
 * @param flag a bitmask flag
 * @receiver Long being used to store bitmask values
 *
 * @return true if the flag is set, false otherwise
 */

fun Long.hasFlag(flag: Long): Boolean {
    return (this and flag) == flag
}

/**
 * Convenience extension function for working with bitmask flags. Toggles a flag within the mask
 */
fun Long.toggleFlag(flag: Long) : Long {
    return if(hasFlag(flag)) {
        this.and(flag.inv())
    }else {
        this.or(flag)
    }
}

