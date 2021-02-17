package com.ustadmobile.core.io

import com.ustadmobile.core.io.ext.putFile
import com.ustadmobile.door.ext.md5Sum
import com.ustadmobile.door.ext.writeToFile
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ConcatenatedInputStream2Test {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    lateinit var tmpFile1: File

    lateinit var tmpFile2: File

    lateinit var tmpConcatenateDataFile: File


    @Before
    fun setup() {
        tmpFile1 = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic0.jpg")
                .writeToFile(tmpFile1)

        tmpFile2 = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic1.jpg")
                .writeToFile(tmpFile2)

        tmpConcatenateDataFile = temporaryFolder.newFile()

        val concatOut = ConcatenatedOutputStream2(FileOutputStream(tmpConcatenateDataFile))
        concatOut.putFile(tmpFile1, 0)
        concatOut.putFile(tmpFile2, 0)
        concatOut.flush()
        concatOut.close()
    }

    @Test
    fun givenConcatenatedInput_whenLastEntryRead_thenGetNextEntryShouldReturnNull() {
        val concatIn = ConcatenatedInputStream2(FileInputStream(tmpConcatenateDataFile))
        concatIn.getNextEntry()
        concatIn.getNextEntry()
        val lastEntry = concatIn.getNextEntry()
        Assert.assertNull("Last entry is null when end of stream is reached", lastEntry)
    }

    @Test
    fun givenConcatenatedInput_whenGetNextEntryCalledBeforeReadingFirstEntry_thenShouldSkipAndReadSecondEntry() {
        val concatIn = ConcatenatedInputStream2(FileInputStream(tmpConcatenateDataFile))
        concatIn.getNextEntry()

        val secondEntry = concatIn.getNextEntry()
        val tmpOutFile = temporaryFolder.newFile()
        FileOutputStream(tmpOutFile).use {
            concatIn.copyTo(it)
            it.flush()
        }

        concatIn.close()

        Assert.assertArrayEquals("MD5sum of second entry matches original second entry (e.g. first " +
                "entry was skipped OK)", tmpFile2.md5Sum, tmpOutFile.md5Sum)
        Assert.assertArrayEquals("MD5Sum of file written matches entry", tmpFile2.md5Sum,
                secondEntry!!.md5)
    }


    @Test(expected = ConcatenatedDataIntegrityException::class)
    fun givenInputThatDoesNotMatchMd5Proivded_whenGetNextEntryCalled_thenShouldThrowDataIntegrityException() {
        val concatOut = ConcatenatedOutputStream2(FileOutputStream(tmpConcatenateDataFile),
                verifyMd5 = false)

        //Put the wrong data ... the md5sum from file2 is used here for file1's data
        concatOut.putNextEntry(ConcatenatedEntry(tmpFile2.md5Sum, 0, tmpFile1.length(), tmpFile1.length(), 0L))
        FileInputStream(tmpFile1).use { fileIn ->
            fileIn.copyTo(concatOut)
            concatOut.flush()
        }

        concatOut.putNextEntry(ConcatenatedEntry(tmpFile1.md5Sum, 0, tmpFile2.length(), tmpFile2.length(), 0L))
        FileInputStream(tmpFile2).use { fileIn ->
            fileIn.copyTo(concatOut)
            concatOut.flush()
        }

        concatOut.close()

        val concatIn = ConcatenatedInputStream2(FileInputStream(tmpConcatenateDataFile))
        concatIn.getNextEntry()

        val secondEntry = concatIn.getNextEntry()
    }



}