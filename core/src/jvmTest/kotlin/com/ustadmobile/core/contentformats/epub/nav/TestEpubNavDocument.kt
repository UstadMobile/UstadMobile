package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.junit.Assert
import org.junit.Test
import org.kmp.io.KMPPullParserException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class TestEpubNavDocument {

    @Test
    @Throws(KMPPullParserException::class, IOException::class)
    fun givenValidDoc_whenParsed_thenPropertiesShouldMatchFile() {
        val navDoc = EpubNavDocument()
        val docIn = javaClass.getResourceAsStream("TestEPUBNavDocument-valid.xhtml")

        navDoc.load(
                UstadMobileSystemImpl.instance.newPullParser(docIn, "UTF-8"))

        Assert.assertNotNull("Navigation doc has found table of contents", navDoc.toc)
        Assert.assertEquals("Navigation doc has 7 children", 7,
                navDoc.toc!!.size().toLong())
    }


    @Test
    fun givenValidNcxDoc_whenParsed_thenPropertiesShouldMatch() {
        val navDoc = EpubNavDocument()
        val docIn = javaClass.getResourceAsStream("TestEpubNcx.ncx")

        navDoc.load(
                UstadMobileSystemImpl.instance.newPullParser(docIn, "UTF-8"))

        Assert.assertNotNull("Navigation doc has found ncx", navDoc.ncxNavMap)
        Assert.assertEquals("NCX has 35 children", 35, navDoc.ncxNavMap!!.size())
    }

    @Test
    @Throws(KMPPullParserException::class, IOException::class)
    fun givenDocLoaded_whenSerializedAndReloaded_thenShouldBeTheSame() {
        val navDoc = EpubNavDocument()
        val docIn = javaClass.getResourceAsStream("TestEPUBNavDocument-valid.xhtml")
        navDoc.load(
                UstadMobileSystemImpl.instance.newPullParser(docIn, "UTF-8"))

        val serializer = UstadMobileSystemImpl.instance.newXMLSerializer()
        val bout = ByteArrayOutputStream()
        serializer.setOutput(bout, "UTF-8")
        navDoc.serialize(serializer)
        bout.flush()


        val loadedDoc = EpubNavDocument()
        val loadedXpp = UstadMobileSystemImpl.instance.newPullParser(
                ByteArrayInputStream(bout.toByteArray()), "UTF-8")
        loadedDoc.load(loadedXpp)

        Assert.assertEquals("Loaded and reserialized docs have same toc id",
                navDoc.toc!!.id, loadedDoc.toc!!.id)
        Assert.assertEquals("Loaded and reserialized tocs have same number of child entries",
                navDoc.toc!!.size().toLong(), loadedDoc.toc!!.size().toLong())
    }

}
