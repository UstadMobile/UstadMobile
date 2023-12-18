package com.ustadmobile.libcache.io

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import kotlin.random.Random

class RangeInputStreamTest {

    @Test
    fun givenValidRange_whenRead_thenWillMatch() {
        val randomBytes = Random.Default.nextBytes(20000)

        val randomSlice1 = RangeInputStream(
            ByteArrayInputStream(randomBytes), 0, 8999
        ).readAllBytes()

        Assert.assertArrayEquals(
            randomBytes.sliceArray(IntRange(0, 8999)),
            randomSlice1
        )

        val randomSlice2 = RangeInputStream(
            ByteArrayInputStream(randomBytes), 9000, 17999
        ).readAllBytes()

        Assert.assertArrayEquals(
            randomBytes.sliceArray(IntRange(9000, 17999)),
            randomSlice2
        )

        val randomSlice3 = RangeInputStream(
            ByteArrayInputStream(randomBytes), 18000, 19999
        ).readAllBytes()
        Assert.assertArrayEquals(
            randomBytes.sliceArray(IntRange(18000, 19999)),
            randomSlice3
        )
    }

    @Test
    fun givenValidRange_whenReadInSameIncrementAsBuffer_thenWillMatch() {
        val randomBytes = Random.Default.nextBytes(20000)

        val randomSlice1 = RangeInputStream(
            ByteArrayInputStream(randomBytes), 0, 8191
        ).readAllBytes()

        Assert.assertArrayEquals(
            randomBytes.sliceArray(IntRange(0, 8191)),
            randomSlice1
        )

        val randomSlice2 = RangeInputStream(
            ByteArrayInputStream(randomBytes), 8192, 16383
        ).readAllBytes()

        Assert.assertArrayEquals(
            randomBytes.sliceArray(IntRange(8192, 16383)),
            randomSlice2
        )

        val randomSlice3 = RangeInputStream(
            ByteArrayInputStream(randomBytes), 16383, 19999
        ).readAllBytes()
        Assert.assertArrayEquals(
            randomBytes.sliceArray(IntRange(16383, 19999)),
            randomSlice3
        )
    }

}