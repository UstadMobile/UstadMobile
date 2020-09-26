package com.ustadmobile.sharedse.impl.http

import com.ustadmobile.port.sharedse.impl.http.EpubHtmlFilterSerializer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.*
import org.kxml2.io.KXmlParser
import org.kxml2.io.KXmlSerializer
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.IOException
import java.nio.charset.Charset

/**
 * Created by mike on 11/4/17.
 */

class TestHtmlFilterSerializer {

    private lateinit var di: DI

    private lateinit var serializer: EpubHtmlFilterSerializer

    @Before
    fun setup() {
        di = DI {
            bind<XmlPullParserFactory>() with singleton {
                XmlPullParserFactory.newInstance().also {
                    it.isNamespaceAware = true
                }
            }

            bind<XmlPullParser>() with provider {
                instance<XmlPullParserFactory>().newPullParser()
            }

            bind<XmlSerializer>() with provider {
                KXmlSerializer()
            }
        }

        serializer = EpubHtmlFilterSerializer(di)
    }

    @Test
    fun givenXhtmlWithEntities_whenFiltered_thenShouldParseSuccessfully() {
        val `in` = javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/epub-page2.xhtml")
        serializer.setIntput(`in`)
        serializer.scriptSrcToAdd = "/path/to/script"
        val filteredInput = serializer.output
        val filteredStr = String(filteredInput, Charset.defaultCharset())
        Assert.assertNotNull(filteredStr)
    }

    @Test
    fun givenValidXhtmlPage_whenFiltered_thenShouldParseSuccessfully() {
        val `in` = javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/epub-page2.xhtml")
        serializer.setIntput(`in`)
        serializer.scriptSrcToAdd = "/path/to/script"
        val filteredInput = serializer.output
        val filteredStr = String(filteredInput, Charset.defaultCharset())
        Assert.assertNotNull(filteredStr)
    }
}
