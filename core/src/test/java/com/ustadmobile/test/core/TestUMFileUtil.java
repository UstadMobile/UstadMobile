package com.ustadmobile.test.core;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMFileUtil;

import org.junit.Assert;
import org.junit.Test;

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

}
