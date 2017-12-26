package com.ustadmobile.test.sharedse;

import com.ustadmobile.test.core.buildconfig.TestConstants;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by mike on 5/10/17.
 */

@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestResumableHttpDownload.class,
        TestMountedZipHandler.class
})

public abstract class SharedSeTestSuite {


    /**
     * The remote test slave device. This can be modified using the @BeforeClass (e.g. by the
     * mock environment)
     */
    public static String REMOTE_SLAVE_SERVER = TestConstants.TEST_REMOTE_SLAVE_SERVER;

}
