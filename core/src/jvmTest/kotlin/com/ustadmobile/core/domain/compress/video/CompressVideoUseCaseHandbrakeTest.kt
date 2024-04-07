package com.ustadmobile.core.domain.compress.video

import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExtractMediaMetadataUseCaseMediaInfo
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertTrue

class CompressVideoUseCaseHandbrakeTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var workingDir: File

    private lateinit var extractMediaMetadataUseCase: ExtractMediaMetadataUseCase

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Before
    fun setup() {
        workingDir = temporaryFolder.newFolder()
        extractMediaMetadataUseCase = ExtractMediaMetadataUseCaseMediaInfo(
            mediaInfoPath = "/usr/bin/mediainfo",
            workingDir = workingDir,
            json = json
        )

    }

    @Test
    fun givenValidVideoFile_whenCompressed_thenWillOutputVideoWithSameLength() {
        val videoExtracted = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")
        val tmpDir = temporaryFolder.newFolder("tmpdir")
        val videoFile = File(tmpDir, "BigBuckBunny.mp4")
        videoExtracted.renameTo(videoFile)

        val useCase = CompressVideoUseCaseHandbrake(
            workingDir = workingDir,
            extractMediaMetadataUseCase = extractMediaMetadataUseCase,
            json = json,
        )

        runBlocking {
            val result = useCase(
                fromUri = videoFile.toDoorUri().toString()
            )

            val outputFile = DoorUri.parse(result?.uri!!).toFile()
            assertTrue(outputFile.exists())
            assertTrue(outputFile.length() > 0)
        }
    }

}