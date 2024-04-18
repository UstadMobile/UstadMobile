package com.ustadmobile.core.domain.compress.video

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import io.github.aakira.napier.Napier


data class DisplayDimensions(val width: Int, val height: Int)

fun MediaExtractor.displayDimensionsOrNull(): DisplayDimensions? {
    for(i in 0 until trackCount) {
        val mediaFormat = getTrackFormat(i)
        val mime= mediaFormat.getString(MediaFormat.KEY_MIME)
        if(mime?.startsWith("video/") == true) {
            mediaFormat.containsKey("display-width")
            if(Build.VERSION.SDK_INT >= 29) {
                Napier.v { "keys: ${mediaFormat.keys}" }
            }

            if(mediaFormat.containsKey("display-width") && mediaFormat.containsKey("display-height")) {
                return DisplayDimensions(
                    width = mediaFormat.getInteger("display-width"),
                    height = mediaFormat.getInteger("display-height")
                )
            }
        }
    }

    return null
}
