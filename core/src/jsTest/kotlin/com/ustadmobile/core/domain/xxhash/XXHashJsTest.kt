package com.ustadmobile.core.domain.xxhash

import com.ustadmobile.core.util.ext.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This test checks that we get the same result using XXHasher on Javascript as we get on JVM/Android
 */
class XXHashJsTest {

    @Test
    fun test() {
        val hasher = XXStringHasherJs()
        assertEquals(-6241840830881246551L, hasher.hash("http://adlnet.gov/expapi/verbs/progressed"))
    }


    @Test
    fun givenLongArray_whenInvoked_thenWillHash() {
        val longsList = listOf(1L, 42L, 50L)
        val factory = XXHasher64FactoryJs()
        val hasher = factory.newHasher(0)
        longsList.forEach {
            hasher.update(it.toByteArray())
        }
        assertEquals(3152173942070070583L, hasher.digest())
    }
}