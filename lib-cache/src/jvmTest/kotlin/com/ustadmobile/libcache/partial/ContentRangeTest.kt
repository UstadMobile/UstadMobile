package com.ustadmobile.libcache.partial

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentRangeTest {

    /**
     * Tets parsing a range that has a set start and end as per
     *
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Range#single_byte_ranges_and_cors-safelisted_requests
     * "The following example requests the first 500 bytes of a resource:
     *
     * Range: bytes=0-499"
     *
     */
    @Test
    fun givenValidRange_whenParsed_thenMatches() {
        val range = ContentRange.parseRangeHeader(
            "bytes=0-499", 1000)
        assertEquals(0L, range.fromByte)
        assertEquals(499L, range.toByte)

        assertEquals(500L, range.contentLength)
        assertEquals(1_000L, range.totalBytes)
    }

    @Test
    fun givenRangeWithStartToEnd_whenParsed_thenMatches() {
        val range = ContentRange.parseRangeHeader(
            "bytes=900-", 1000L)
        assertEquals(900L, range.fromByte)
        assertEquals(999L, range.toByte)
        assertEquals(100L, range.contentLength)
        assertEquals(1_000L, range.totalBytes)
    }

    /**
     * Test request for the last bytes as per
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Range
     * "Alternatively, if it's unknown how large a resource is, the last n bytes can be requested using a suffix range of -n:
     *
     * Range: bytes=-100
     *"
     */
    @Test
    fun givenBlankStart_whenParsed_thenMatches() {
        val range = ContentRange.parseRangeHeader("bytes=-100", 1000)
        assertEquals(901, range.fromByte)
        assertEquals(1000, range.toByte)
        assertEquals(100, range.contentLength)
        assertEquals(1000, range.totalBytes)
    }

    @Test
    fun givenRangeWhereLastByteAfterTotalBytes_whenParsed_thenThrowsRangeNotSatisfiableException() {
        try {
            //Because the last byte pos is inclusive, the last valid value is 999
            ContentRange.parseRangeHeader("bytes=900-1000", 1000L)
            throw IllegalStateException("Should have thrown exception by now")
        }catch (e: Exception) {
            assertTrue(e is RangeRequestNotSatisfiableException)
        }
    }

    @Test
    fun givenRangeWhereStartIsAfterLast_whenParsed_thenThrowsRangeNotSatisfiableException() {
        try {
            ContentRange.parseRangeHeader("bytes=100-50", 1000L)
            throw IllegalStateException("Should have thrown exception by now")
        } catch (e: Exception) {
            assertTrue(e is RangeRequestNotSatisfiableException)
        }
    }


}