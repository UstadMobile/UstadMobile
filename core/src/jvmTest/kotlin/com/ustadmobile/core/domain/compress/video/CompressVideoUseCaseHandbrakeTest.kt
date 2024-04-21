package com.ustadmobile.core.domain.compress.video

import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExecuteMediaInfoUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExtractMediaMetadataUseCaseMediaInfo
import com.ustadmobile.core.util.mockGetStoragePathUseCase
import com.ustadmobile.core.util.requireHandBrakeCommand
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.util.test.initNapierLog
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

    private lateinit var executeMediaInfoUseCase: ExecuteMediaInfoUseCase

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Before
    fun setup() {
        workingDir = temporaryFolder.newFolder()

        executeMediaInfoUseCase = ExecuteMediaInfoUseCase(
            mediaInfoPath = "/usr/bin/mediainfo",
            workingDir = workingDir,
            json = json,
        )
        extractMediaMetadataUseCase = ExtractMediaMetadataUseCaseMediaInfo(
            executeMediaInfoUseCase = executeMediaInfoUseCase,
            getStoragePathForUrlUseCase = mockGetStoragePathUseCase(),
        )

    }

    @Test
    fun givenValidVideoFile_whenCompressed_thenWillOutputVideoWithSameLength() {
        initNapierLog()
        //FlatPak cannot access /tmp, so copy to the working directory and be 100% sure to delete
        val videoFile = File(System.getProperty("user.dir"), "tmp-bigbuckbunny.mp4")
        try {
            this::class.java.getResourceAsStream("/com/ustadmobile/core/container/BigBuckBunny.mp4")!!.use { inStream ->
                inStream.writeToFile(videoFile)
            }


            val useCase = CompressVideoUseCaseHandbrake(
                handbrakeCommand = requireHandBrakeCommand(),
                workDir = File(System.getProperty("user.dir")),
                extractMediaMetadataUseCase = extractMediaMetadataUseCase,
                json = json,
            )

            runBlocking {
                val result = useCase(
                    fromUri = videoFile.toDoorUri().toString()
                )

                val outputFile = DoorUri.parse(result?.uri!!).toFile()
                assertTrue(outputFile.exists(), "Output file $outputFile should exist")
                assertTrue(outputFile.length() > 0,
                    "Output file $outputFile should have file size > 0 (actual ${outputFile.length()}bytes)")

                outputFile.delete()
            }
        }finally {
            videoFile.delete()
        }
    }

}