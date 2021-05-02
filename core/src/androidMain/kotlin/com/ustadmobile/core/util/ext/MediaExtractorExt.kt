package com.ustadmobile.core.util.ext

import android.media.MediaExtractor
import android.media.MediaFormat
import com.ustadmobile.core.catalog.contenttype.AudioCodecInfo

/**
 * Get the audio sample rate and channel count for the first audio track in the stream
 */
fun MediaExtractor.getFirstAudioCodecInfo() : AudioCodecInfo{
    for(i in 0 until trackCount){
        val trackFormat = getTrackFormat(i)
        val mimeType = trackFormat.getString(MediaFormat.KEY_MIME)
        if(mimeType?.startsWith("audio/") == true) {
            val sampleRate = trackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            if(sampleRate > 0)
                return AudioCodecInfo(sampleRate, channelCount)
        }
    }

    return AudioCodecInfo(-1, -1)
}

/**
 * Run a block of code, then release the MediaExtractor (using a try-catch to ensure that the
 * MediaExtractor is always released)
 */
inline fun <R> MediaExtractor.useThenRelease(block: (MediaExtractor) -> R) : R {
    try {
        return block(this)
    }catch(t: Throwable) {
        throw t
    }finally {
        release()
    }
}

