package com.ustadmobile.port.android.impl.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.ustadmobile.codec2.Codec2
import com.ustadmobile.codec2.Codec2Decoder
import com.ustadmobile.core.util.UMIOUtils
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

class Codec2Player(private val inStream: InputStream, private val pos: Long) : Runnable {
    private val playing: AtomicBoolean = AtomicBoolean()

    fun play() {
        playing.set(true)
        Thread(this).start()
    }

    fun stop() {
        playing.set(false)
    }


    override fun run() {
        var track: AudioTrack? = null
        var codec2: Codec2Decoder? = null
        try {
            val bufSize = AudioTrack.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT)

            track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    8000,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufSize,
                    AudioTrack.MODE_STREAM)
            track.play()

            codec2 = Codec2Decoder(inStream, Codec2.CODEC2_MODE_3200)
            val headerSize = 7
            val frameDurationMs = codec2.samplesPerFrame.toFloat() / 8f
            val framesToSkip = Math.round(pos / frameDurationMs)
            inStream.skip((headerSize + framesToSkip * codec2.inputBufferSize).toLong())
            var buffer = codec2.readFrame()
            while (playing.get() && buffer != null) {
                track.write(buffer.array(), 0, buffer.capacity())
                println("Wrote track data")
                buffer = codec2.readFrame()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            track?.release()
            codec2?.destroy()
            UMIOUtils.closeInputStream(inStream)
        }

    }
}
