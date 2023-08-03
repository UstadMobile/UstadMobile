package com.ustadmobile.core.contentformats.opds

import org.junit.Assert
import org.junit.Test
import org.xmlpull.v1.XmlPullParserFactory

class TestOpds {

    @Test
    fun givenValidOpds_whenLoaded_thenShouldHavePropertiesLoaded() {

        val opfIn = javaClass.getResourceAsStream("root.xml")
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(opfIn, "UTF-8")
        val feed = OpdsFeed()
        feed.loadFromParser(parser)

        Assert.assertEquals("Global Digital Library - Book Catalog", feed.title)
        Assert.assertEquals("6be123b3-6f14-46e8-8c3e-8d759aa24a45", feed.id)
        Assert.assertEquals(15, feed.entryList.size)

    }

}