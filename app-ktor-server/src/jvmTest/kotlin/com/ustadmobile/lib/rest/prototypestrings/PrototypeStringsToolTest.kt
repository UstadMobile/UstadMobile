package com.ustadmobile.lib.rest.prototypestrings

import com.ustadmobile.door.ext.writeToFile
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PrototypeStringsToolTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenPrototypeInput_whenRun_willReplaceEnglishStrings(){
//        val epgzIn = temporaryFolder.newFile()
//        this::class.java.getResourceAsStream("/test-mockup.epgz").writeToFile(epgzIn)

        val epgzIn = File("/home/mike/Documents/UstadMobile/ownCloud/Hawk/Hawk2.0.5.epgz")


        val outFile = File("/home/mike/Hawk2.0.5.epgz")
        PrototypeStringsTool().substituteStrings(epgzIn, "tg", outFile)
    }

}