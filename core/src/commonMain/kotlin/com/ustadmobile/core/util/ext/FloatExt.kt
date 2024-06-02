package com.ustadmobile.core.util.ext

import kotlin.math.round

/**
 * Remove the decimal point if this float has nothing after the decimal point.
 */
fun Float.toDisplayString(): String {
    val strVal = toString()
    return if(round(this) == this) {
        strVal.substringBefore('.')
    }else {
        strVal
    }
}