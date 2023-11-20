package com.ustadmobile.core.contentformats.epub.ocf

import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class TestContainerParse  {

    @OptIn(ExperimentalXmlUtilApi::class)
    @Test
    fun givenValidContainer_whenParsed_thenMatches() {
        val ocf = """
            <container xmlns="urn:oasis:names:tc:opendocument:xmlns:container" version="1.0">
            <rootfiles>
            <rootfile full-path="OEBPS/package.opf" media-type="application/oebps-package+xml"/>
            </rootfiles>
            </container>
        """.trimIndent()
        val xml = XML {
            defaultPolicy {
                unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }

        val container = xml.decodeFromString(
            deserializer = Container.serializer(),
            string = ocf,
        )

        val rootFile = container.rootFiles!!.rootFiles.first()
        assertEquals("OEBPS/package.opf", rootFile.fullPath)
        assertEquals("application/oebps-package+xml", rootFile.mediaType)
    }

}