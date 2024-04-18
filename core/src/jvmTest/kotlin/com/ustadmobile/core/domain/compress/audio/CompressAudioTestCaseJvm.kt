package com.ustadmobile.core.domain.compress.audio

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompressAudioTestCaseJvm {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenValidAudioInputFile_whenInvoked_thenWillCompress() {
        val compressor = CompressAudioUseCaseJvm(
            workDir = temporaryFolder.newFolder(),
            soxPath = CompressAudioUseCaseJvm.findSox()!!.absolutePath
        )

        val audioFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/river.mp3",
            fileName = "soundfile.mp3")

        runBlocking {
            val result = compressor.invoke(
                fromUri = audioFile.toDoorUri().toString()
            )
            assertNotNull(result)
            assertTrue(DoorUri.parse(result.uri).toFile().length() > 0)
        }
    }
}
