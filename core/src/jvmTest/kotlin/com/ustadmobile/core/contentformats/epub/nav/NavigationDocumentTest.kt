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
        assertEquals(7, listItems.size)

        //Assert found the sub item
        assertNotNull(listItems.first().orderedList?.listItems?.first())

        assertEquals("toc", navDoc.bodyElement.navigationElements.first().epubType)
    }

}