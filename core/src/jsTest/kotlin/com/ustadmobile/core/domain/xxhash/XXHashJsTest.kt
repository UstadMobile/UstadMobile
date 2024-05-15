package com.ustadmobile.core.domain.xxhash

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This test checks that we get the same result using XXHasher on Javascript as we get on JVM/Android
 */
class XXHashJsTest {

    @Test
    fun test() {
        val hasher = XXHasherJs()
        assertEquals(-6241840830881246551L, hasher.hash("http://adlnet.gov/expapi/verbs/progressed"))
    }

}