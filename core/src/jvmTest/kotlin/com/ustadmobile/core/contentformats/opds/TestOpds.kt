package com.ustadmobile.core.contentformats.opds

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.junit.Assert
import org.junit.Test

class TestOpds {

    @Test
    fun givenValidOpds_whenLoaded_thenShouldHavePropertiesLoaded() {

        val opfIn = javaClass.getResourceAsStream("root.xml")
        val parser = UstadMobileSystemImpl.instance.newPullParser()
        parser.setInput(opfIn, "UTF-8")
        var feed = OpdsFeed()
        feed.loadFromParser(parser)

        Assert.assertEquals("Global Digital Library - Book Catalog", feed.title)
        Assert.assertEquals("6be123b3-6f14-46e8-8c3e-8d759aa24a45", feed.id)
        Assert.assertEquals(15, feed.entryList.size)
        Assert.assertNotNull(feed.updated)

    }

}