package com.ustadmobile.lib.util

import org.junit.Assert
import org.junit.Test

class TestAcceptEncodingUtil {

    @Test
    fun givenEncodingWithQualityValue_whenParsed_thenShouldBeInMap() {
        val encodingHeader = "br;q=1.0, gzip;q=0.8, *;q=0.1"
        val parsed = parseAcceptedEncoding(encodingHeader)

        Assert.assertEquals("Parsed br encoding value",
                parsed.acceptableEncodings["br"], 1.0F)

        Assert.assertEquals("Parsed gzip encoding value",
                parsed.acceptableEncodings["gzip"], 0.8F)

        Assert.assertEquals("Parsed gzip encoding value",
                parsed.acceptableEncodings["*"], 0.1F)
    }

    @Test
    fun givenEncodingWithoutQualityValue_whenParsed_thenShouldBeInMap() {
        val encodingHeader = "gzip, compress, br"
        val parsed = parseAcceptedEncoding(encodingHeader)

        Assert.assertEquals("Parsed br encoding value",
                parsed.acceptableEncodings["br"], 1.0F)

        Assert.assertEquals("Parsed gzip encoding value",
                parsed.acceptableEncodings["gzip"], 1.0F)

        Assert.assertEquals("Parsed gzip encoding value",
                parsed.acceptableEncodings["compress"], 1.0F)
    }

    @Test
    fun givenEncodingNotSupported_whenIsEncodingAcceptableCalled_thenShouldBeFalse() {
        val encodingHeader = "identity;q=1, *;q=0"

        val parsed = parseAcceptedEncoding(encodingHeader)

        Assert.assertFalse("Gzip not supported by accept-encoding identity;q=1, *;q=0",
            parsed.isEncodingAcceptable("gzip"))
    }

    @Test
    fun givenEncodingDirectlySupported_whenIsEncodingAcceptableCalled_thenShouldBeTrue()  {
        val encodingHeader = "gzip, compress, br"

        val parsed = parseAcceptedEncoding(encodingHeader)

        Assert.assertTrue("Gzip is supported by gzip, compress, br",
                parsed.isEncodingAcceptable("gzip"))
    }

    @Test
    fun givenEncodingSupportedByStar_whenIsEncodingAcceptableCalled_thenShouldBeTrue()  {
        val encodingHeader = "*"

        val parsed = parseAcceptedEncoding(encodingHeader)

        Assert.assertTrue("Gzip is supported by *",
                parsed.isEncodingAcceptable("gzip"))
    }





}