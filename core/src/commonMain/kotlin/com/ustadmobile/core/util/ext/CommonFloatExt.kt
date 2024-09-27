package com.ustadmobile.core.util.ext

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

fun Float.roundTo(decimalPlaces: Int = 0): Float {
    val factor = 10.0.pow(decimalPlaces.toDouble())
    return ((this * factor).roundToInt() / factor).toFloat()
}

/**
 * Remove the decimal point if this float has nothing after the decimal point.
 */
fun Float.toDisplayString(
    decimalPlaces: Int = 2
): String {
    val strVal = toString()
    return if(round(this) == this) {
        strVal.substringBefore('.')
    }else {
        this.roundTo(decimalPlaces).toString().removeSuffix("0")
    }
}