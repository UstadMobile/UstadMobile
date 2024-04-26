package com.ustadmobile.core.domain.extractvideothumbnail

import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExtractVideoThumbnailUseCaseJvmTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenValidVideo_whenInvoked_thenWillSaveValidThumbnail() {
        val videoTmp = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")
        val tmpDir = temporaryFolder.newFolder("video")
        val videoFile = File(tmpDir, "BigBuckBunny.mp4")
        videoTmp.renameTo(videoFile)
        runBlocking {
            val thumbnailFile = temporaryFolder.newFile()
            ExtractVideoThumbnailUseCaseJvm().invoke(
                videoUri = File("/home/mike/Downloads/tmp-contentfiles/Bus.mp4").toDoorUri(),
                position = 0.3f,
                destinationFilePath = thumbnailFile.absolutePath
            )

            assertTrue(thumbnailFile.exists())
            //Test that the image can be read
            assertNotNull(ImageIO.read(thumbnailFile))
        }

    }

}