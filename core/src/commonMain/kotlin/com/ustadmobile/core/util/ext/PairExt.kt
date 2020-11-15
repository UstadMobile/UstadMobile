package com.ustadmobile.core.util.ext

import kotlin.math.abs


/**
 * Standard Low and High Quality Resolutions as per Android recommended
 * @ https://developer.android.com/guide/topics/media/media-formats
 */
val VALID_RESOLUTIONS = listOf(Pair(480,360), Pair(176,144), Pair(360,480), Pair(144,176))

fun Pair<Int, Int>.variance(other: Pair<Int, Int>): Int = abs(this.first - other.first) + abs(this.second - other.second)

fun Pair<Int, Int>.fitWithin(): Pair<Int, Int>{
    val maxWidth = 640
    val maxHeight = 360

    val originalWidth = first
    val originalHeight = second

    if(originalWidth > originalHeight && originalWidth > maxWidth) {

        val ratio = maxWidth.toFloat() / originalWidth.toFloat()
        val newPair =  Pair(maxWidth, (originalHeight * ratio).toInt())
        return VALID_RESOLUTIONS.minBy { it.variance(newPair) } ?: newPair
    }else if(originalHeight > originalWidth && originalHeight > maxHeight){

        val ratio = maxHeight.toFloat() / originalHeight.toFloat()
        val newPair = Pair((originalWidth * ratio).toInt(), maxHeight)
        return VALID_RESOLUTIONS.minBy { it.variance(newPair) } ?: newPair
    }
    return VALID_RESOLUTIONS.minBy { it.variance(this) } ?: this
}