package com.ustadmobile.core.domain.extractmediametadata.mediainfo

import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.json.MediaInfoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class ExtractMediaMetadataUseCaseMediaInfo (
    private val mediaInfoPath: String,
    private val workingDir: File,
    private val json: Json,
): ExtractMediaMetadataUseCase {

    override suspend fun invoke(
        file: File
    ): ExtractMediaMetadataUseCase.MediaMetaData = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder(
                listOf(
                    mediaInfoPath, "--Output=JSON", file.absolutePath)
                )
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(workingDir)
                .start()
            val processOutput = process.inputStream.bufferedReader().use { it.readText() }
            val mediaInfo = json.decodeFromString(MediaInfoResult.serializer(), processOutput)
            val videoTrack = mediaInfo.media?.track?.firstOrNull {
                it.type.equals("video", true)
            }

            ExtractMediaMetadataUseCase.MediaMetaData(
                duration = mediaInfo.media?.track?.maxOfOrNull {
                    ((it.duration?.toFloat() ?: 0f) * 1000).toLong()
                } ?: 0,
                hasVideo = videoTrack != null,
                storageHeight = videoTrack?.height?.toInt() ?: -1,
                storageWidth = videoTrack?.width?.toInt() ?: -1,
                aspectRatio = videoTrack?.displayAspectRatio?.toFloat() ?: -1f,
            )
        }catch(e: Throwable) {
            throw e
        }
    }
}