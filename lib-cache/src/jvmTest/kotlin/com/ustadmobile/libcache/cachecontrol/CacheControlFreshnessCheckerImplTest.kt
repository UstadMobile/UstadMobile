package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CacheControlFreshnessCheckerImplTest {

    @Test
    fun givenResponseWithinMaxAge_whenChecked_isFresh() {
        val responseHeaders = iHeadersBuilder {
            header("cache-control", "max-age=86400")
        }
        val requestHeaders = iHeadersBuilder {
            header("cache-control", "cache")
        }

        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            responseLastValidated = systemTimeInMillis() - 60000,
            responseFirstStoredTime = systemTimeInMillis() - 60000
        )
        assertTrue(status.isFresh, "Resource within maxage is fresh")
    }

    @Test
    fun givenResponseHasMustRevalidate_whenChecked_isStale() {
        val etag = "tagged"
        val lastModified = "Tue, 22 Feb 2022 20:20:20 GMT"
        val responseHeaders = iHeadersBuilder {
            header("cache-control", "must-revalidate")
            header("etag", etag)
            header("last-modified", lastModified)
        }
        val requestHeaders = iHeadersBuilder {

        }

        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            responseLastValidated = systemTimeInMillis(),
            responseFirstStoredTime = systemTimeInMillis()
        )

        assertFalse(status.isFresh)
        assertEquals(status.ifNoneMatch, etag)
        assertEquals(status.ifNotModifiedSince, lastModified)
    }

    @Test
    fun givenResponseIsImmutable_whenChecked_isFresh() {
        val responseHeaders = iHeadersBuilder {
            header("cache-control", "immutable")
        }
        val requestHeaders = iHeadersBuilder {  }

        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            responseLastValidated = systemTimeInMillis() - 60000,
            responseFirstStoredTime = systemTimeInMillis() - 60000
        )

        assertTrue(status.isFresh)
    }

    @Test
    fun givenRequestHasNoCacheDirective_whenChecked_isStale() {
        val responseHeaders = iHeadersBuilder {
            header("cache-control", "max-age=86400")
        }
        val requestHeaders = iHeadersBuilder {
            header("cache-control", "no-cache")
        }
        val cachedResponseTime = systemTimeInMillis() - 60000
        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            responseLastValidated = cachedResponseTime,
            responseFirstStoredTime = cachedResponseTime,
        )

        assertFalse(status.isFresh)
    }


}