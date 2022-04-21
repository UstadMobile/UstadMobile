package com.ustadmobile.core.util.ext

import kotlin.math.pow
import kotlin.math.roundToInt

fun Float.roundTo(decimalPlaces: Int = 0): Float {
    val factor = 10.0.pow(decimalPlaces.toDouble())
    return ((this * factor).roundToInt() / factor).toFloat()
}