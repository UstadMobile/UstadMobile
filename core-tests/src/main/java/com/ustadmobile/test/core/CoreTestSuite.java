package com.ustadmobile.test.core;

import com.ustadmobile.test.core.catalog.contenttype.TestXapiPackageTypePlugin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by mike on 5/17/17.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestHTTPCacheDir.class,
        TestImageLoader.class,
        TestDirectoryScanner.class,
        TestXapiPackageTypePlugin.class,
        TestUstadJSOPDSFeed.class,
        TestUMFileUtil.class
})
public abstract class CoreTestSuite {
}
