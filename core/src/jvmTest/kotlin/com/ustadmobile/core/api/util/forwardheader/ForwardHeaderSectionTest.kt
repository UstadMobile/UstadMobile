package com.ustadmobile.core.api.util.forwardheader

import org.junit.Assert
import org.junit.Test

class ForwardHeaderTest {

    /**
     * Test headers to parse as per Mozilla examples
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded
     */
    @Test
    fun givenValidHeadersShouldParse() {
        val example1Result = parseForwardHeader("For=\"[2001:db8:cafe::17]:4711\"")
        Assert.assertEquals("[2001:db8:cafe::17]:4711", example1Result.first().forVal)

        val example2Result = parseForwardHeader("for=192.0.2.60;proto=http;by=203.0.113.43")
        Assert.assertEquals("192.0.2.60", example2Result.first().forVal)
        Assert.assertEquals("http", example2Result.first().protoVal)
        Assert.assertEquals("203.0.113.43", example2Result.first().byVal)

        val example3Result = parseForwardHeader("for=192.0.2.43, for=198.51.100.17")
        Assert.assertEquals("192.0.2.43", example3Result[0].forVal)
        Assert.assertEquals("198.51.100.17", example3Result[1].forVal)
    }

}