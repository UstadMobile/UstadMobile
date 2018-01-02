package com.ustadmobile.test.core;

import com.ustadmobile.test.core.catalog.contenttype.TestXapiPackageTypePlugin;
import com.ustadmobile.test.core.impl.TestUstadJSOPF;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by mike on 5/17/17.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestImageLoader.class,
        TestDirectoryScanner.class,
        TestXapiPackageTypePlugin.class,
        TestUstadJSOPDSFeed.class,
        TestUstadJSOPDSEntry.class,
        TestUMFileUtil.class,
        TestUstadJSOPF.class,
        TestUmCalendarUtil.class
})
public abstract class CoreTestSuite {
}
