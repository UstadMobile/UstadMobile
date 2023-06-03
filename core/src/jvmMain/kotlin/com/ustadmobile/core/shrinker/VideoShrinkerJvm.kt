package com.ustadmobile.core.shrinker

import com.ustadmobile.core.shrink.ShrinkConfig
import com.ustadmobile.core.shrink.ShrinkProgressListener
import com.ustadmobile.core.shrink.Shrinker
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder
import net.bramp.ffmpeg.job.FFmpegJob

class VideoShrinkerJvm: Shrinker {
    override suspend fun shrink(
        srcUri: DoorUri,
        destinationUri: DoorUri,
        config: ShrinkConfig,
        progressListener: ShrinkProgressListener?,
    ) {
        val srcFile = srcUri.toFile()
        val destFile = srcUri.toFile()

        //TODO here: use ffmpeg to compress --> done
        // TODO: need to discuss config implementation details

        /*
        val pb = ProcessBuilder(
            "ffmpeg",
            "-i",
            srcFile.absolutePath.toString(),
            "-vf",
            "-quality",
            "good",
            "-c:v",
            "libvpx-vp9",
            "libvorbis",
            destFile.absolutePath.toString()
        )

        pb.redirectErrorStream(true)
        val process = pb.start()
        process.waitFor()
        // progressListener = pb.start()
        // progressListener.waitFor()
        */

        val ffmpeg = FFmpeg("ffmpeg");
        val ffprobe = FFprobe("ffprobe");

        val builder: FFmpegOutputBuilder = FFmpegBuilder()

            .setInput(srcFile.toString())     // Filename, or a FFmpegProbeResult
            .overrideOutputFiles(true) // Override the output if it exists

            .addOutput(destFile.toString())   // Filename for the destination
            .setFormat("mp4")        // Format is inferred from filename, or can be set
            .setTargetSize(250_000)  // Aim for a 250KB file

            .disableSubtitle()       // No subtiles

            .setAudioChannels(1)         // Mono audio
            .setAudioCodec("aac")        // using the aac codec
            .setAudioSampleRate(48_000)  // at 48KHz
            .setAudioBitRate(32768)      // at 32 kbit/s

            .setVideoCodec("libx264")     // Video using x264
            .setVideoFrameRate(24, 1)     // at 24 frames per second
            .setVideoResolution(640, 480) // at 640x480 resolution

            .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL).build() // Allow FFmpeg to use experimental specs

        val executor = FFmpegExecutor(ffmpeg, ffprobe)

        // Run a one-pass encode
                executor.createJob(builder).run()

        // Or run a two-pass encode (which is better quality at the cost of being slower)
                executor.createTwoPassJob(builder).run()

    }


}
