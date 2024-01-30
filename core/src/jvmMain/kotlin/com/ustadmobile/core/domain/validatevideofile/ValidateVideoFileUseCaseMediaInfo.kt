package com.ustadmobile.core.domain.validatevideofile

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Use the MediaInfo command to validate if a file is really a video. In theory the VLC command
 * could get some info, however the documentation is not up to date and results are unreliable.
 *
 */
class ValidateVideoFileUseCaseMediaInfo(
    private val mediaInfoPath: String,
    private val workingDir: File,
    private val json: Json,
): ValidateVideoFileUseCase {

    @Suppress("unused")
    @Serializable
    class MediaInfoResult(
        val creatingLibrary: MediaInfoCreatingLibrary? = null,
        val media: MediaInfoMediaElement? = null,
    )

    @Suppress("unused")
    @Serializable
    class MediaInfoCreatingLibrary(
        val name: String? = null,
        val version: String? = null,
        val url: String? = null,
    )


    @Serializable
    class MediaInfoMediaElement(
        val track: List<MediaInfoTrack>
    )

    @Suppress("unused")
    @Serializable
    class MediaInfoTrack(
        @SerialName("@type")
        val type: String,
        @SerialName("VideoCount")
        val videoCount: String? = null,
        @SerialName("AudioCount")
        val audioCount: String? = null,
        @SerialName("Format")
        val format: String? = null,
    )


    override suspend fun invoke(
        videoUri: DoorUri
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val videoFile = videoUri.toFile()
            val process = ProcessBuilder(listOf(mediaInfoPath, "--Output=JSON",
                    videoFile.absolutePath))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(workingDir)
                .start()
            val processOutput = process.inputStream.bufferedReader().use { it.readText() }
            val mediaInfo = json.decodeFromString(MediaInfoResult.serializer(), processOutput)
            mediaInfo.media?.track?.any { (it.videoCount?.toIntOrNull() ?: 0) > 0 } == true
        }catch(e: Throwable) {
            Napier.w("ValidateVideoFileUseCase: error processing $videoUri", e)
            false
        }
    }
}