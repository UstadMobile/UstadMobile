package com.ustadmobile.lib.rest.remainingstrings

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RemainingStringsToolTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun givenLanguageCanFindRemainingStrings() {
        val remainingStringsTool = RemainingStringsTool()
        val remainingStrings = remainingStringsTool.findRemainingStrings("ar")
        val outputFolder = tempFolder.newFolder()
        remainingStringsTool.serializeRemainingStrings(remainingStrings, outputFolder)
    }

}