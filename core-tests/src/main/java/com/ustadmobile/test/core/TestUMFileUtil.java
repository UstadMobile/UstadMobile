package com.ustadmobile.test.core;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.URLTextUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by mike on 6/18/17.
 */

public class TestUMFileUtil {

    @Test
    public void testAppendExtensionToFilenameIfNeeded() {
        Assert.assertEquals("Will append file extension when needed", "foo.bar.epub",
                UMFileUtil.appendExtensionToFilenameIfNeeded("foo.bar",
                UstadJSOPDSFeed.TYPE_EPUBCONTAINER));
        Assert.assertEquals("Will leave filename when extension is already there", "foo.epub",
                UMFileUtil.appendExtensionToFilenameIfNeeded("foo.epub",
                UstadJSOPDSFeed.TYPE_EPUBCONTAINER));
        Assert.assertEquals("Will leave filename when extension is unknown", "foo.bar",
                UMFileUtil.appendExtensionToFilenameIfNeeded("foo.bar",
                "application/x-foo-bar"));

    }

    @Test
    public void testRemoveExtension() {
        Assert.assertEquals("Can remove extension", UMFileUtil.removeExtension("filename.txt"),
                "filename");
        Assert.assertEquals("If file has no extension, same value is returned",
                UMFileUtil.removeExtension("filename"), "filename");
    }


    @Test
    public void testSplitCombinedViewArguments() {
        Hashtable combinedArgs = new Hashtable();
        String catalogUrl0 = "http://www.ustadmobile.com/files/s4s/index.testnewcat.opds";
        combinedArgs.put("catalog-0-url", catalogUrl0);
        combinedArgs.put("catalog-1-url", "bar");

        Vector splitArgs = UMFileUtil.splitCombinedViewArguments(combinedArgs, "catalog", '-');
        Hashtable args0 = (Hashtable)splitArgs.get(0);
        Assert.assertEquals("Catalog 0 arg is as expected", args0.get("url"), catalogUrl0);
    }

}
