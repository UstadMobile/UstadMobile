package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.libcache.headers.headersBuilder
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResponseCacheabilityCheckerImplTest {

    @Test
    fun given200OkResponse_whenChecked_isCacheable() {
        assertTrue(
            ResponseCacheabilityCheckerImpl().invoke(
                200,
                headersBuilder {  },
            )
        )
    }

    @Test
    fun givenPartialResponse_whenChecked_notCacheable() {
        assertFalse(
            ResponseCacheabilityCheckerImpl().invoke(
                206,
                headersBuilder {  },
            )
        )
    }

    @Test
    fun givenNoStoreInHeader_whenChecked_notCacheable() {
        assertFalse(
            ResponseCacheabilityCheckerImpl().invoke(
                200,
                headersBuilder {
                    header("cache-control", "no-store")
                }
            )
        )
    }



}