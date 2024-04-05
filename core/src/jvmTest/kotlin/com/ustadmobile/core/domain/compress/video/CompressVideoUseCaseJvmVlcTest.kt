package com.ustadmobile.core.domain.compress.video

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertTrue

class CompressVideoUseCaseJvmVlcTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var workingDir: File

    @Before
    fun setup() {
        workingDir = temporaryFolder.newFolder()
    }

    @Test
    fun givenValidVideoFile_whenCompressed_thenWillOutputVideoWithSameLength() {
        val videoExtracted = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")
        val tmpDir = temporaryFolder.newFolder("tmpdir")
        val videoFile = File(tmpDir, "BigBuckBunny.mp4")
        videoExtracted.renameTo(videoFile)

        val useCase = CompressVideoUseCaseJvmVlc(
            workingDir = workingDir
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