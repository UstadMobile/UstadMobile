package com.ustadmobile.core.io

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

class ConcatenatingInputStreamTest {

    val testResourcesList = (1 .. 3).map { "/com/ustadmobile/core/container/testfile$it.png" }

    val testByteArrs = testResourcesList.map {
        this@ConcatenatingInputStreamTest::class.java.getResourceAsStream(it).readBytes()
    }

    val testMd5Sums = testByteArrs.map {
        val md5 = MessageDigest.getInstance("MD5")
        md5.update(it)
        md5.digest()
    }

    @Test
    fun givenConcatenatingInputStreamBytes_whenReadWithConcatenatedOutput_thenContentsShouldBeTheSame() {
        val concatenatingInput = ConcatenatingInputStream(testByteArrs.mapIndexed {index, it ->
            ConcatenatedPartSource({ByteArrayInputStream(it)}, it.size.toLong(), it.size.toLong(),
                    testMd5Sums[index])
        })

        val concatenatedBytes = concatenatingInput.readBytes()

        assertConcatenatedInputIsTheSame(concatenatedBytes)
    }

    @Test
    fun givenConcatenatingInputStreamBytes_whenSkippedInMiddleAndSplit_thenShouldBeTheSameWhenBackTogether() {
        val concatenatedBytes = makeConcatenatingInputStreamWithSplit(150*1024)
        assertConcatenatedInputIsTheSame(concatenatedBytes)
    }

    @Test
    fun givenConcatenatingInputStream_whenSkippedOnBoundary_thenShouldBeTheSameWhenBackTogether() {
        val splitPoint = (3*24) + 4 + testByteArrs[0].size.toLong()
        val concatenatedBytes = makeConcatenatingInputStreamWithSplit(splitPoint)
        assertConcatenatedInputIsTheSame(concatenatedBytes)
    }

    @Test
    fun givenConcatenatingInputStream_whenSkippedBeforeBoundary_thenShouldBeTheSameWhenBackTogether() {
        val splitPoint = (3*24) + 4 + testByteArrs[0].size.toLong() -1
        val concatenatedBytes = makeConcatenatingInputStreamWithSplit(splitPoint)
        assertConcatenatedInputIsTheSame(concatenatedBytes)
    }

    @Test
    fun givenConcatenatingInputStream_whenSkippedToLength_thenShouldBeTheSameWhenBackTogether() {
        val splitPoint = 76 + testByteArrs.sumBy { it.size }
        val concatenatedBytes = makeConcatenatingInputStreamWithSplit(splitPoint.toLong())
        assertConcatenatedInputIsTheSame(concatenatedBytes)
    }

    //This should never really happen
    //@Test
    fun givenConcatenatingInputStream_whenSkippedBeyondLength_thenShouldBeTheSameWhenBackTogether() {
        val splitPoint = 76 + testByteArrs.sumBy { it.size } + 1000
        val concatenatedBytes = makeConcatenatingInputStreamWithSplit(splitPoint.toLong())
        assertConcatenatedInputIsTheSame(concatenatedBytes)
    }

    private fun makeConcatenatingInputStreamWithSplit(splitPoint: Long) : ByteArray{
        val concatenatingInput1 = ConcatenatingInputStream(testByteArrs.mapIndexed { index, it ->
            ConcatenatedPartSource({ByteArrayInputStream(it)}, it.size.toLong(), it.size.toLong(),
                    testMd5Sums[index])
        })

        val byteArrOut = ByteArrayOutputStream()
        var readCount = 0
        val buf = ByteArray(8* 1024)
        var bytesRead = 0
        while(readCount < splitPoint && bytesRead != -1) {
            bytesRead = concatenatingInput1.read(buf)
            byteArrOut.write(buf, 0, bytesRead)
            readCount += bytesRead
        }

        concatenatingInput1.close()

        val concatenatingInput2 = ConcatenatingInputStream(testByteArrs.mapIndexed { index, it ->
            ConcatenatedPartSource({ByteArrayInputStream(it)}, it.size.toLong(), it.size.toLong(),
                    testMd5Sums[index])
        })

        concatenatingInput2.skip(readCount.toLong())
        byteArrOut.write(concatenatingInput2.readBytes())
        byteArrOut.flush()
        concatenatingInput2.close()

        return byteArrOut.toByteArray()
    }

    private fun assertConcatenatedInputIsTheSame(concatenatedBytes: ByteArray) {
        val concatenatedInput = ConcatenatedInputStream(ByteArrayInputStream(concatenatedBytes))
        var nextChunk: ConcatenatedPart? = null
        var chunkIndex = 0
        while(concatenatedInput.nextPart().also { nextChunk = it } != null) {
            val chunkData = concatenatedInput.readBytes()
            Assert.assertArrayEquals("Chunk $chunkIndex data is the same",
                    testByteArrs[chunkIndex], chunkData)
            Assert.assertEquals("Length of chunk is correct", chunkData.size,
                    nextChunk!!.length.toInt())
            Assert.assertArrayEquals("Correct md5 sum returned", testMd5Sums[chunkIndex],
                    nextChunk!!.id)
            chunkIndex++
        }

        Assert.assertEquals("Got expected number of chunks", testByteArrs.size, chunkIndex)
        Assert.assertNull("Got null after chunks of file were exhausted", nextChunk)
    }

}