package com.ustadmobile.core.domain.xxhash

import kotlin.test.Test
import kotlin.test.assertEquals

class XXHashJvmTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun givenString_whenInvoked_willHash() {
        val hasher = XXHashCommonJvm()
        val hash = hasher.hash("http://adlnet.gov/expapi/verbs/progressed")
        assertEquals(-6241840830881246551L, hash)
        println(hash.toHexString())
    }

}