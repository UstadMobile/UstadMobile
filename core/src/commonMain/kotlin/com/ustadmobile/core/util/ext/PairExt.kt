package com.ustadmobile.core.util.ext

import kotlin.math.abs

val validResolutions = listOf(Pair(640,360), Pair(320,180))

fun Pair<Int, Int>.variance(other: Pair<Int, Int>): Int = abs(this.first - other.first) + abs(this.second - other.second)

fun Pair<Int, Int>.fitWithin(): Pair<Int, Int>{
    val maxWidth = 640
    val maxHeight = 360

    val originalWidth = first
    val originalHeight = second

    if(originalWidth > originalHeight && originalWidth > maxWidth) {

        val ratio = maxWidth.toFloat() / originalWidth.toFloat()
        val newPair =  Pair(maxWidth, (originalHeight * ratio).toInt())
        return validResolutions.minBy { it.variance(newPair) } ?: newPair
    }else if(originalHeight > originalWidth && originalHeight > maxHeight){

        val ratio = maxHeight.toFloat() / originalHeight.toFloat()
        val newPair = Pair((originalWidth * ratio).toInt(), maxHeight)
        return validResolutions.minBy { it.variance(newPair) } ?: newPair
    }
    return validResolutions.minBy { it.variance(this) } ?: this
}