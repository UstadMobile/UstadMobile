package com.ustadmobile.core.shrink

import com.ustadmobile.core.shrinker.VideoShrinkerJvm
import com.ustadmobile.door.ext.toDoorUri
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertTrue

class JvmVideoShrinkerTest {

    @JvmField
    @Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun givenValidVideo_whenShrinkCalled_thenShouldShrink() {
        val inTmpFile = tempFolder.newFile()
        val outTmpFile = tempFolder.newFile()

        //need to put video in test-resources/com/ustadmobile/core/contenttype/
        inTmpFile.outputStream().use {
            this::class.java.getResourceAsStream("/com/ustadmobile/core/container/BigBuckBunny.mp4").copyTo(it)
            it.flush()
        }

        val videoShrinker = VideoShrinkerJvm()


        runBlocking {
            videoShrinker.shrink(inTmpFile.toDoorUri(), outTmpFile.toDoorUri(), ShrinkConfig())
        }

        //Assert that theres a valid video file in outTmpFile
        assertTrue(outTmpFile.exists())


    }

}