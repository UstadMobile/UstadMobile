package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class TestEpubNavDocument {

    lateinit var xppFactory: XmlPullParserFactory

    @Before
    fun setup() {
        xppFactory = XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = true
        }
    }

    @Test
    fun givenValidDoc_whenParsed_thenPropertiesShouldMatchFile() {
        val navDoc = EpubNavDocument()
        val docIn = javaClass.getResourceAsStream("TestEPUBNavDocument-valid.xhtml")

        val xppParser = xppFactory.newPullParser()
        xppParser.setInput(docIn, "UTF-8")
        navDoc.load(xppParser)

        Assert.assertNotNull("Navigation doc has found table of contents", navDoc.toc)
        Assert.assertEquals("Navigation doc has 7 children", 7,
                navDoc.toc!!.size().toLong())
    }


    @Test
    fun givenValidNcxDoc_whenParsed_thenPropertiesShouldMatch() {
        val navDoc = EpubNavDocument()
        val docIn = javaClass.getResourceAsStream("TestEpubNcx.ncx")

        val xppParser = xppFactory.newPullParser().also {
            it.setInput(docIn, "UTF-8")
        }

        navDoc.load(xppParser)

        Assert.assertNotNull("Navigation doc has found ncx", navDoc.ncxNavMap)
        Assert.assertEquals("NCX has 35 children", 35, navDoc.ncxNavMap!!.size())
    }

    @Test
    fun givenDocLoaded_whenSerializedAndReloaded_thenShouldBeTheSame() {
        val navDoc = EpubNavDocument()
        val docIn = javaClass.getResourceAsStream("TestEPUBNavDocument-valid.xhtml")
        val xppParser = xppFactory.newPullParser().also {
            it.setInput(docIn, "UTF-8")
        }
        navDoc.load(xppParser)

        val serializer = xppFactory.newSerializer()
        val bout = ByteArrayOutputStream()
        serializer.setOutput(bout, "UTF-8")
        navDoc.serialize(serializer)
        bout.flush()


        val loadedDoc = EpubNavDocument()
        val loadedXpp = xppFactory.newPullParser().also {
            it.setInput(ByteArrayInputStream(bout.toByteArray()), "UTF-8")
        }
        loadedDoc.load(loadedXpp)

        Assert.assertEquals("Loaded and reserialized docs have same toc id",
                navDoc.toc!!.id, loadedDoc.toc!!.id)
        Assert.assertEquals("Loaded and reserialized tocs have same number of child entries",
                navDoc.toc!!.size().toLong(), loadedDoc.toc!!.size().toLong())
    }

}
