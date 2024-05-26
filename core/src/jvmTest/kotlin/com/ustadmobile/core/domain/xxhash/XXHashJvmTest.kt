package com.ustadmobile.core.domain.xxhash

import com.ustadmobile.core.util.ext.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This test is here primarily to ensure we get consistent results eg. the same values on JVM/Android and JS
 */
class XXHashJvmTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun givenString_whenInvoked_willHash() {
        val hasher = XXStringHasherCommonJvm()
        val hash = hasher.hash("http://adlnet.gov/expapi/verbs/progressed")
        assertEquals(-6241840830881246551L, hash)
        println(hash.toHexString())
    }

    @Test
    fun givenLongArray_whenInvoked_thenWillHash() {
        val longsList = listOf(1L, 42L, 50L)
        val factory = XXHasher64FactoryCommonJvm()
        val hasher = factory.newHasher(0)
        longsList.forEach {
            hasher.update(it.toByteArray())
        }
        assertEquals(3152173942070070583L, hasher.digest())
    }

}