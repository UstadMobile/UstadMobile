package com.ustadmobile.core.io

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.security.MessageDigest

class ConcatenatingInputStreamTest {

    val testResourcesList = (1 .. 3).map { "/com/ustadmobile/core/container/testfile$it.png" }

    @Test
    fun givenConcatenatingInputStreamBytes_whenReadWithConcatenatedOutput_thenContentsShouldBeTheSame() {
        val testByteArrs = testResourcesList.map {
            this@ConcatenatingInputStreamTest::class.java.getResourceAsStream(it).readBytes()
        }

        val testMd5Sums = testByteArrs.map {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(it)
            md5.digest()
        }

        val concatenatingInput = ConcatenatingInputStream(testByteArrs.mapIndexed {index, it ->
            ConcatenatedPartSource({ByteArrayInputStream(it)}, it.size.toLong(),
                    testMd5Sums[index])
        })

        val concatenatedBytes = concatenatingInput.readBytes()

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