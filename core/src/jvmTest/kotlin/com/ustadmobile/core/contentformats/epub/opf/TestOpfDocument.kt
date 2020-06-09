package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.junit.Assert
import org.junit.Test
import org.kmp.io.KMPPullParserException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Created by mike on 10/17/17.
 */

class TestOpfDocument {

    @Test
    @Throws(IOException::class, KMPPullParserException::class)
    fun givenValidOpf_whenLoaded_thenShouldHavePropertiesFromOpfFile() {
        val opfIn = javaClass.getResourceAsStream("TestOpfDocument-valid.opf")
        val parser = UstadMobileSystemImpl.instance.newPullParser()
        parser.setInput(opfIn, "UTF-8")
        val opf = OpfDocument()
        opf.loadFromOPF(parser)
        Assert.assertEquals("Title as expected", "The Little Chicks", opf.title)
        Assert.assertEquals("Id as expected", "202b10fe-b028-4b84-9b84-852aa766607d", opf.id)
        Assert.assertTrue("Spine loaded", opf.getSpine().size > 0)
        Assert.assertEquals("Language loaded", "en-US", opf.getLanguages()[0])
        Assert.assertEquals("Cover image as expected", "cover.png", opf.getCoverImage("")!!.href)
        Assert.assertEquals("Loaded author 1 as expected", "Benita Rowe",
                opf.getCreator(0)?.creator)
        Assert.assertEquals("Loaded author 1 as expected -id", "author1",
                opf.getCreator(0)?.id)
        Assert.assertEquals("Loaded author 2 as expected", "Mike Dawson",
                opf.getCreator(1)?.creator)
        Assert.assertEquals("Loaded mime type as expected for page", "application/xhtml+xml",
                opf.getMimeType("Page_1.xhtml"))
    }

    @Test
    @Throws(IOException::class, KMPPullParserException::class)
    fun givenOpfLoaded_whenSerializedThenLoaded_shouldBeEqual() {
        val opfIn = javaClass.getResourceAsStream("TestOpfDocument-valid.opf")
        val parser = UstadMobileSystemImpl.instance.newPullParser()
        parser.setInput(opfIn, "UTF-8")
        val opf = OpfDocument()
        opf.loadFromOPF(parser)

        val bout = ByteArrayOutputStream()
        val serializer = UstadMobileSystemImpl.instance.newXMLSerializer()
        serializer.setOutput(bout, "UTF-8")
        opf.serialize(serializer)
        bout.flush()

        val loadedOpf = OpfDocument()
        val xpp = UstadMobileSystemImpl.instance.newPullParser(
                ByteArrayInputStream(bout.toByteArray()), "UTF-8")
        loadedOpf.loadFromOPF(xpp)

        Assert.assertEquals("Original and reserialized title is the same", opf.title,
                loadedOpf.title)
        Assert.assertEquals("Original and reserialized id is the same", opf.id,
                loadedOpf.id)
        Assert.assertEquals("Original and reserialized opf has same number of manifest entries",
                opf.getManifestItems().size.toLong(), loadedOpf.getManifestItems().size.toLong())
        for (item in opf.getManifestItems().values) {
            val loadedItem = loadedOpf.getManifestItems()[item.id]
            Assert.assertNotNull("Manifest item id #" + item.id +
                    " present in reserialized manifest", loadedItem)
            Assert.assertEquals("Item id # " + item.id + " same href",
                    item.href, loadedItem?.href)
            Assert.assertEquals("Item id #" + item.id + " same mime type",
                    item.mediaType, loadedItem?.mediaType)
        }

        Assert.assertEquals("Original and reserialized TOC has same navitem",
                opf.navItem!!.id, loadedOpf.navItem!!.id)

    }

}
