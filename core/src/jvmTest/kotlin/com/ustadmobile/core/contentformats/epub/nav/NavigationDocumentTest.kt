package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.io.ext.readString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalXmlUtilApi::class)
class NavigationDocumentTest {

    @Test
    fun givenValidNavigationDocument_whenParsed_thenMatchesExpectedVals() {
        val navXhtmlStr = this::class.java.getResourceAsStream(
            "/com/ustadmobile/core/contentformats/epub/nav/TestEPUBNavDocument-valid.xhtml"
        )!!.readString()

        val xml = XML {
            defaultPolicy {
                unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }

        val navDoc = xml.decodeFromString(
            NavigationDocument.serializer(), navXhtmlStr
        )

        val listItems = navDoc.bodyElement.navigationElements.first().orderedList.listItems

        //Assert found the expected number of items
        assertEquals(8, listItems.size)

        //Assert found the sub item
        assertNotNull(listItems.first().orderedList?.listItems?.first())

        assertEquals("toc", navDoc.bodyElement.navigationElements.first().epubType)

        assertEquals("Page_1.xhtml", listItems.first().anchor?.href)

        /*
         * Assert that conversion of anchor content to plain text is working as expected in
         *  TestEPUBNavDocument-valid.xhtml the second item contains embedded tags (bold) that are
         *  are put together into plain text
         */
        assertEquals("This is anchor content with embedded tags.",
            listItems[1].anchor?.content,
            message = "Content that was in embedded tags e.g. <b> is converted into plain text"
        )

        assertEquals("This is anchor content with an image description.",
            listItems[2].anchor?.content,
            message = "Anchor content that included an image includes the alt tag text"
        )

        assertEquals("Header only here.",
            listItems.last().span?.content,
            message = "Span content that includes embedded tags is processed as expected")

    }

}