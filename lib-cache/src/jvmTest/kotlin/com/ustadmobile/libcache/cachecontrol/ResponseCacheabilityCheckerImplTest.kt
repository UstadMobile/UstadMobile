package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.ihttp.headers.iHeadersBuilder
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResponseCacheabilityCheckerImplTest {

    @Test
    fun given200OkResponse_whenChecked_isCacheable() {
        assertTrue(
            ResponseCacheabilityCheckerImpl().invoke(
                200,
                iHeadersBuilder {  },
            )
        )
    }

    @Test
    fun givenPartialResponse_whenChecked_notCacheable() {
        assertFalse(
            ResponseCacheabilityCheckerImpl().invoke(
                206,
                iHeadersBuilder {  },
            )
        )
    }

    @Test
    fun givenNoStoreInHeader_whenChecked_notCacheable() {
        assertFalse(
            ResponseCacheabilityCheckerImpl().invoke(
                200,
                iHeadersBuilder {
                    header("cache-control", "no-store")
                }
            )
        )
    }



}