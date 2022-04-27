package com.ustadmobile.core.io.ext

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.Rule
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.zip.CRC32
import org.junit.Assert
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

class InputStreamExtTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()


    @Test
    fun givenInputStream_whenCopyToAndGetDigestsWithGzipEnabled_thenMd5MatchesOriginalSha256MatchesCompressed() {
        val outputTmpFile = temporaryFolder.newFile()

        val byteArrOut = ByteArrayOutputStream()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/testfile1.png")!!
            .copyTo(byteArrOut)
        byteArrOut.flush()
        val inData = byteArrOut.toByteArray()

        val md5Digest = MessageDigest.getInstance("MD5")
        val shaDigest = MessageDigest.getInstance("SHA-256")
        val crc32 = CRC32()

        val checksumResults = runBlocking {
            ByteArrayInputStream(inData).copyToAndGetDigests(outputTmpFile, true,
                md5Digest, shaDigest, crc32)
        }

        val gzipIn = GZIPInputStream(FileInputStream(outputTmpFile))
        val byteArrOut2 = ByteArrayOutputStream()
        gzipIn.copyTo(byteArrOut2)
        byteArrOut2.flush()
        Assert.assertArrayEquals("Inflated data stored matches original data",
            inData, byteArrOut2.toByteArray())

        val originalMd5 = MessageDigest.getInstance("MD5")
            .digest(inData)

        Assert.assertArrayEquals("Got expected original md5 result",
            originalMd5, checksumResults.md5)

        val compressedData = outputTmpFile.readBytes()
        val compressedSha256 = MessageDigest.getInstance("SHA-256")
            .digest(compressedData)

        Assert.assertArrayEquals("Got expected sha256 result", compressedSha256,
            checksumResults.sha256)

        val compressedCrc = CRC32().also {
            it.update(compressedData)
        }.value

        Assert.assertEquals("Got expected CRC", compressedCrc, checksumResults.crc32)




    }


    @Test
    fun givenInputStream_whenCopyToAndGetDigestsWithGzipDisabled_thenChecksumsShouldMatch() {
        val outputTmpFile = temporaryFolder.newFile()

        val byteArrOut = ByteArrayOutputStream()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/testfile1.png")!!
            .copyTo(byteArrOut)
        byteArrOut.flush()
        val inData = byteArrOut.toByteArray()

        val md5Digest = MessageDigest.getInstance("MD5")
        val shaDigest = MessageDigest.getInstance("SHA-256")
        val crc32 = CRC32()

        val checksumResults = runBlocking {
            ByteArrayInputStream(inData).copyToAndGetDigests(outputTmpFile, false,
                md5Digest, shaDigest, crc32)
        }

        val originalMd5 = MessageDigest.getInstance("MD5")
            .digest(inData)

        Assert.assertArrayEquals("Got expected original md5 result",
            originalMd5, checksumResults.md5)

        val originalSha256 = MessageDigest.getInstance("SHA-256")
            .digest(inData)

        Assert.assertArrayEquals("Got expected sha256", originalSha256,
            checksumResults.sha256)

        val originalCrc = CRC32().also {
            it.update(inData)
        }.value

        Assert.assertEquals("Got expected crc", originalCrc, checksumResults.crc32)
    }

}