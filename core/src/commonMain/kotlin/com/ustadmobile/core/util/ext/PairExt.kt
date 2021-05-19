package com.ustadmobile.core.util.ext

import kotlin.math.abs


/**
 * Standard Low and High Quality Resolutions as per Android recommended
 * @ https://developer.android.com/guide/topics/media/media-formats
 */
val VALID_RESOLUTIONS = listOf(
        Pair(480,360), Pair(360,480),
        Pair(176,144), Pair(144,176),
        Pair(640, 360), Pair(360, 640),
        Pair(320, 180), Pair(180, 320),
        Pair(480, 270), Pair(270, 480),
        Pair(480, 480),
        Pair(240, 240))

fun Pair<Int, Int>.variance(other: Pair<Int, Int>): Int = abs(this.first - other.first) + abs(this.second - other.second)

fun Pair<Int, Int>.fitWithin(maxDimension: Int = 640): Pair<Int, Int>{
    val originalWidth = first
    val originalHeight = second

    if(originalWidth >= originalHeight && originalWidth > maxDimension) {

        val ratio = maxDimension.toFloat() / originalWidth.toFloat()
        val newPair =  Pair(maxDimension, (originalHeight * ratio).toInt())
        return VALID_RESOLUTIONS.minByOrNull { it.variance(newPair) } ?: newPair
    }else if(originalHeight > originalWidth && originalHeight > maxDimension){

        val ratio = maxDimension.toFloat() / originalHeight.toFloat()
        val newPair = Pair((originalWidth * ratio).toInt(), maxDimension)
        return VALID_RESOLUTIONS.minByOrNull { it.variance(newPair) } ?: newPair
    }
    return VALID_RESOLUTIONS.minByOrNull { it.variance(this) } ?: this
}