package com.ustadmobile.core.domain.extractmediametadata.mediainfo

import com.ustadmobile.core.domain.extractmediametadata.mediainfo.json.MediaInfoResult
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Execute MediaInfo and return the result as deserialized Json entity.
 */
class ExecuteMediaInfoUseCase(
    private val mediaInfoPath: String,
    private val workingDir: File,
    private val json: Json,
) {

    /**
     * Run MediaInfo and deserialize the Json result
     */
    operator fun invoke(file: File): MediaInfoResult {
        val process = ProcessBuilder(
            listOf(
                mediaInfoPath, "--Output=JSON", file.absolutePath)
        )
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .directory(workingDir)
            .start()
        val processOutput = process.inputStream.bufferedReader().use { it.readText() }
        val processResult = process.waitFor()
        if(processResult != 0) {
            throw IllegalStateException(
                "ExecuteMediaInfoUseCase: MediaInfo exited with non-zero status code: $processResult"
            )
        }

        return json.decodeFromString(MediaInfoResult.serializer(), processOutput)
    }

}