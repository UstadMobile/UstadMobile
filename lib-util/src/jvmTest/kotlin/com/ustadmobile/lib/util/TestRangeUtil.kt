package com.ustadmobile.lib.util

import org.junit.Test
import kotlin.test.assertEquals

class TestRangeUtil  {

    @Test
    fun givenValidRangeHeader_whenParseRangeRequestHeaderCalled_thenShouldReturnRange() {
        val rangeResponse = parseRangeRequestHeader("bytes=0-1023", 2048)
        assertEquals(206, rangeResponse.statusCode, "Valid range request provides 206 response code")
        assertEquals(0, rangeResponse.fromByte, "Range response first byte = 0")
        assertEquals(1023, rangeResponse.toByte, "Range response last byte = 1023")
        assertEquals("1024", rangeResponse.responseHeaders["Content-Length"],
                "Content-Length = 1024")
        assertEquals( "bytes 0-1023/2048", rangeResponse.responseHeaders["Content-Range"])
        assertEquals("bytes", rangeResponse.responseHeaders["Accept-Ranges"])
    }

    @Test
    fun givenZeroToEndRangeHeader_whenParseRangeRequestHeaderCalled_thenShouldReturnWholeFileRange() {
        val rangeResponse = parseRangeRequestHeader("bytes=0-", 23328)
        assertEquals(206, rangeResponse.statusCode, "Valid range request provides 206 response code")
        assertEquals(0, rangeResponse.fromByte, "Range response first byte = 0")
        assertEquals(23327, rangeResponse.toByte, "Range response last byte = 23327")
        assertEquals( "bytes 0-23327/23328", rangeResponse.responseHeaders["Content-Range"])
    }

    @Test
    fun givenUnsatisfiableRangeHeader_whenParseRangeRequestHeaderCalled_tehnShouldReturnRangeNotSatisfiable() {
        val rangeResponse = parseRangeRequestHeader("bytes=0-5000", 2048)
        assertEquals(416, rangeResponse.statusCode)
    }

    @Test
    fun givenInvalidHeader_whenParseRangeRequestCalled_thenShouldReturnBadRequest() {
        assertEquals(400,
                parseRangeRequestHeader("unicorns five thousand", 2048).statusCode,
                "Given invalid range header status code = 400")
    }

}