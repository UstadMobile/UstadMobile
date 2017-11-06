package com.ustadmobile.test.core;

import com.ustadmobile.core.catalog.DirectoryScanner;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mike on 9/9/17.
 */

public class TestDirectoryScanner {

    @Before
    public void copyEntryFile() throws IOException {
        UMTestUtil.copyResourceToStorageDir("/com/ustadmobile/test/core/thelittlechicks.epub");
    }

    @Test
    public void testDirectoryScanner() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Assert.assertTrue(true);
        UMStorageDir[] storageDirs = impl.getStorageDirs(CatalogPresenter.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());
        String dirPath = storageDirs[0].getDirURI();
        DirectoryScanner scanner = new DirectoryScanner();
        UstadJSOPDSFeed directoryFeed = scanner.scanDirectory(dirPath,
                impl.getCacheDir(CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext()),
                "test", "test-id",
                CatalogPresenter.SHARED_RESOURCE, null, null, PlatformTestUtil.getTargetContext());
        Assert.assertNotNull(directoryFeed);


    }

}
