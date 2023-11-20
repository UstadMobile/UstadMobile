package com.ustadmobile.core.contentformats.epub.ncx

import com.ustadmobile.core.io.ext.readString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalXmlUtilApi::class)
class NcxDocumentTest {

    @Test
    fun givenValidNcxDoc_whenParsed_thenShouldProvideExpectedValues() {
        val ncxText = this::class.java.getResourceAsStream(
            "/com/ustadmobile/core/contentformats/epub/nav/TestEpubNcx.ncx"
        )!!.readString()

        val xml = XML {
            defaultPolicy {
                unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }

        val ncxDoc = xml.decodeFromString(
            deserializer = NcxDocument.serializer(),
            ncxText
        )

        assertEquals("[Место заголовка]", ncxDoc.docTitle.texts.first().content)
        val firstNavPoint = ncxDoc.navMap.navPoints.first()
        assertEquals("Муқаддима", firstNavPoint.navLabels.first().text.content)
        assertEquals("Text/Section0004.xhtml", firstNavPoint.content.src)

        //Note: in test doc, NavPoint6 is embedded within navpoint5, so top level size is 35, not 36
        assertEquals(35, ncxDoc.navMap.navPoints.size)
    }

}