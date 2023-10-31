package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.util.test.ext.newFileFromResource
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalXmlUtilApi::class)
class TestPackageParse {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenValidPackageDocThenShouldParse() {
        val opfDoc = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contentformats/epub/opf/TestOpfDocument-valid.opf")
        val xml = XML {
            defaultPolicy {
                unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }

        val opf = xml.decodeFromString(Package.serializer(), opfDoc.readText())
        assertEquals("A Book", opf.metadata.titles.firstOrNull()?.content)
        assertEquals("202b10fe-b028-4b84-9b84-852aa766607d", opf.uniqueIdentifierContent())

    }
}