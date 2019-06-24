package com.ustadmobile.sharedse.impl.http

import com.ustadmobile.port.sharedse.impl.http.EpubHtmlFilterSerializer
import org.junit.Assert
import org.junit.Test
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.nio.charset.Charset

/**
 * Created by mike on 11/4/17.
 */

class TestHtmlFilterSerializer {

    @Test
    @Throws(IOException::class, XmlPullParserException::class)
    fun testSerializer() {
        val serializer = EpubHtmlFilterSerializer()
        val `in` = javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/epub-page.html")
        serializer.setIntput(`in`)
        serializer.scriptSrcToAdd = "/path/to/script"
        val filteredInput = serializer.output
        val filteredStr = String(filteredInput, Charset.defaultCharset())
        Assert.assertNotNull(filteredStr)
    }
}
