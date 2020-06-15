package com.ustadmobile.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by mike on 6/18/17.
 *
 * TODO: Refactor this to use test name conventions
 */
class TestUMFileUtil {


    @Test
    fun testUMFileUtilJoin() {
        assertEquals("Can handle basic join with to single slash",
                "testpath/somefile.txt",
                UMFileUtil.joinPaths(*arrayOf("testpath/", "/somefile.txt")))
        assertEquals("Will not remove first slash", "/testpath/somefile.txt",
                UMFileUtil.joinPaths(*arrayOf("/testpath/", "/somefile.txt")))
        assertEquals("Will not remove trailing slash", "/testpath/somedir/",
                UMFileUtil.joinPaths(*arrayOf("/testpath/", "/somedir/")))
    }

    @Test
    fun testUMFileUtilResolveLink() {
        assertEquals("Absolute path returns same path back",
                "http://www.server2.com/somewhere",
                UMFileUtil.resolveLink("http://server1.com/some/place",
                        "http://www.server2.com/somewhere"))
        assertEquals("Can resolve prtocol only link",
                "http://www.server2.com/somewhere",
                UMFileUtil.resolveLink("http://server1.com/some/place",
                        "//www.server2.com/somewhere"))
        assertEquals("Can resolve relative to server link",
                "http://server1.com/somewhere",
                UMFileUtil.resolveLink("http://server1.com/some/place",
                        "/somewhere"))
        assertEquals("Can handle basic relative path",
                "http://server1.com/some/file.jpg",
                UMFileUtil.resolveLink("http://server1.com/some/other.html",
                        "file.jpg"))
        assertEquals("Can handle .. in relative path",
                "http://server1.com/file.jpg",
                UMFileUtil.resolveLink("http://server1.com/some/other.html",
                        "../file.jpg"))

        assertEquals("Can handle base link with no folder", "images/thumb.png",
                UMFileUtil.resolveLink("content.opf", "images/thumb.png"))

    }


}
