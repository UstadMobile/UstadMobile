package com.ustadmobile.core.util.ext

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class LongExtTest {

    private val longs = listOf(
        0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE
    ) +  (1..100).map { Random.nextLong() }

    //Test conversion to/from ByteArray
    @Test
    fun givenListOfLongs_whenConvertedToFromByteArray_thenWillBeEqual() {
        longs.forEach {
            val bytes = it.toByteArray()
            val fromBytes = bytes.toLong()
            assertEquals(it, fromBytes)
        }
    }

}