package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libcache.headers.headersBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CacheControlFreshnessCheckerImplTest {

    @Test
    fun givenResponseWithinMaxAge_whenChecked_isFresh() {
        val responseHeaders = headersBuilder {
            header("cache-control", "max-age=86400")
        }
        val requestHeaders = headersBuilder {
            header("cache-control", "cache")
        }

        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders, responseHeaders, systemTimeInMillis() - 60000,
            systemTimeInMillis() - 60000
        )
        assertTrue(status.isFresh, "Resource within maxage is fresh")
    }

    @Test
    fun givenResponseHasMustRevalidate_whenChecked_isStale() {
        val etag = "tagged"
        val lastModified = "Tue, 22 Feb 2022 20:20:20 GMT"
        val responseHeaders = headersBuilder {
            header("cache-control", "must-revalidate")
            header("etag", etag)
            header("last-modified", lastModified)
        }
        val requestHeaders = headersBuilder {

        }

        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders, responseHeaders, systemTimeInMillis(), systemTimeInMillis()
        )

        assertFalse(status.isFresh)
        assertEquals(status.ifNoneMatch, etag)
        assertEquals(status.ifNotModifiedSince, lastModified)
    }

    @Test
    fun givenResponseIsImmutable_whenChecked_isFresh() {
        val responseHeaders = headersBuilder {
            header("cache-control", "immutable")
        }
        val requestHeaders = headersBuilder {  }

        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders, responseHeaders, systemTimeInMillis() - 60000,
            systemTimeInMillis() - 60000
        )

        assertTrue(status.isFresh)
    }

    @Test
    fun givenRequestHasNoCacheDirective_whenChecked_isStale() {
        val responseHeaders = headersBuilder {
            header("cache-control", "max-age=86400")
        }
        val requestHeaders = headersBuilder {
            header("cache-control", "no-cache")
        }
        val cachedResponseTime = systemTimeInMillis() - 60000
        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders, responseHeaders, cachedResponseTime, cachedResponseTime
        )

        assertFalse(status.isFresh)
    }


}