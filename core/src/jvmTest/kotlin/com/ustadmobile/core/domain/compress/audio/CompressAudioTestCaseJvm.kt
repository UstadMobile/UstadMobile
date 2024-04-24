package com.ustadmobile.core.domain.compress.audio

import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExecuteMediaInfoUseCase
import com.ustadmobile.core.test.assertSameMediaDuration
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.util.test.initNapierLog
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompressAudioTestCaseJvm {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private lateinit var executeMediaInfoUseCase : ExecuteMediaInfoUseCase

    private val soxPath = CompressAudioUseCaseSox.findSox()

    @Before
    fun setup() {
        initNapierLog()
        Assume.assumeNotNull(soxPath)

        val mediaInfoPath = SysPathUtil.findCommandInPath("mediainfo")
        Assume.assumeNotNull(mediaInfoPath)

        executeMediaInfoUseCase = ExecuteMediaInfoUseCase(
            mediaInfoPath = mediaInfoPath!!.absolutePath,
            workingDir = temporaryFolder.newFolder(),
            json = json
        )
    }

    @Test
    fun givenValidAudioInputFile_whenInvoked_thenWillCompress() {
        val compressor = CompressAudioUseCaseSox(
            workDir = temporaryFolder.newFolder(),
            executeMediaInfoUseCase = executeMediaInfoUseCase,
            soxPath = soxPath!!.absolutePath
        )

        val audioFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/river.mp3",
            fileName = "soundfile.mp3")

        runBlocking {
            val result = compressor.invoke(
                fromUri = audioFile.toDoorUri().toString()
            )
            assertNotNull(result)
            val resultFile = DoorUri.parse(result.uri).toFile()
            assertSameMediaDuration(executeMediaInfoUseCase, audioFile, resultFile)
            assertTrue(resultFile.length() < audioFile.length())
        }
    }

    @Test
    fun givenMpg123PathNotNullAndInputIsMp3ThenWillInvokeMpg123AndCompressWav() {
        val mpg123Path = SysPathUtil.findCommandInPath("mpg123")
        Assume.assumeNotNull(mpg123Path)

        val compressor = CompressAudioUseCaseSox(
            workDir = temporaryFolder.newFolder(),
            soxPath = soxPath!!.absolutePath,
            executeMediaInfoUseCase = executeMediaInfoUseCase,
            mpg123Path = mpg123Path!!.absolutePath,
        )

        val mp3File = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/river.mp3",
            fileName = "soundfile.mp3")

        runBlocking {
            val result = compressor.invoke(
                fromUri = mp3File.toDoorUri().toString()
            )
            assertNotNull(result)
            val resultFile = DoorUri.parse(result.uri).toFile()
            assertSameMediaDuration(executeMediaInfoUseCase, mp3File, resultFile)
            assertTrue(resultFile.length() < mp3File.length())
        }
    }

    @Test
    fun givenMpg123PathNotNullAndInputIsNotMp3_whenInvoked_thenWillNotUseMpg123() {
        val mpg123Path = SysPathUtil.findCommandInPath("mpg123")
        Assume.assumeNotNull(mpg123Path)

        val compressor = CompressAudioUseCaseSox(
            workDir = temporaryFolder.newFolder(),
            soxPath = soxPath!!.absolutePath,
            executeMediaInfoUseCase = executeMediaInfoUseCase,
            mpg123Path = mpg123Path!!.absolutePath,
        )

        val wavFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/river.wav")

        runBlocking {
            val result = compressor.invoke(
                fromUri = wavFile.toDoorUri().toString()
            )
            assertNotNull(result)
            val resultFile = DoorUri.parse(result.uri).toFile()
            assertSameMediaDuration(executeMediaInfoUseCase, wavFile, resultFile)
            assertTrue(resultFile.length() < wavFile.length())
        }
    }
}
