package com.ustadmobile.core.domain.validatevideofile

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream

class ValidateVideoFileUseCaseFfprobe(
    private val ffprobe: FFprobe,
) : ValidateVideoFileUseCase {

    override suspend fun invoke(videoUri: DoorUri): Boolean = withContext(Dispatchers.IO) {
        try {
            val videoFile = videoUri.toFile()
            val probeResult = ffprobe.probe(videoFile.absolutePath)
            probeResult.getStreams().any {
                it.codec_type == FFmpegStream.CodecType.VIDEO
            }
        }catch(e: Throwable) {
            Napier.w(e) { "ValidateVideoFileUseCaseFfprobe: exception checking $videoUri"}
            false
        }

    }
}