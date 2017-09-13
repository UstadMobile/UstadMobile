package com.ustadmobile.test.core;

import com.ustadmobile.core.catalog.DirectoryScanner;
import com.ustadmobile.core.controller.CatalogController;
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
        InputStream entryIn = getClass().getResourceAsStream(
                "/com/ustadmobile/test/core/thelittlechicks.epub");
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Object context = PlatformTestUtil.getTargetContext();
        UMStorageDir[] storageDirs = impl.getStorageDirs(CatalogController.SHARED_RESOURCE,
                context);
        String outDir = storageDirs[0].getDirURI();
        if(!impl.dirExists(outDir)) {
            impl.makeDirectoryRecursive(outDir);
        }

        String outPath = UMFileUtil.joinPaths(new String[]{outDir, "thelittlechicks.epub"});

        OutputStream fileOut = UstadMobileSystemImpl.getInstance().openFileOutputStream(outPath, 0);
        UMIOUtils.readFully(entryIn, fileOut, 8*1024);
    }

    @Test
    public void testDirectoryScanner() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Assert.assertTrue(true);
        UMStorageDir[] storageDirs = impl.getStorageDirs(CatalogController.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());
        String dirPath = storageDirs[0].getDirURI();
        DirectoryScanner scanner = new DirectoryScanner();
        UstadJSOPDSFeed directoryFeed = scanner.scanDirectory(dirPath, null, "test", "test-id",
                CatalogController.SHARED_RESOURCE, PlatformTestUtil.getTargetContext());
        Assert.assertNotNull(directoryFeed);


    }

}
