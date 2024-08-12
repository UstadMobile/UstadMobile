package com.ustadmobile.core.domain.compress.image

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue


class CompressImageUseCaseJvmTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenInputImage_whenInvoked_thenWillBeResized() {
        val testImage = temporaryFolder.newFileFromResource(javaClass,
            "/com/ustadmobile/core/container/testfile1.png")
        runBlocking {
            val result = CompressImageUseCaseJvm().invoke(
                fromUri = testImage.toDoorUri().toString()
            )
            val resultFile = DoorUri.parse(result!!.uri).toFile()
            assertTrue(resultFile.length() > 0)
            //Validate that the result can be read
            ImageIO.read(resultFile)


        }
    }

}