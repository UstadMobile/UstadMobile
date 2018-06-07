package com.ustadmobile.test.sharedse;

import com.ustadmobile.test.core.buildconfig.TestConstants;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by mike on 5/10/17.
 */

@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestCatalogUriResponder.class,
        TestMountedZipHandler.class
})

public abstract class SharedSeTestSuite {

    /**
     * An entry id for a container file which is present on the test peer (should be downloaded
     * over the local network) - the little chicks.
     */
    public static final String ENTRY_ID_LOCAL = "202b10fe-b028-4b84-9b84-852aa766607d";

    /**
     * An entry id for a container file which is not present on the test peer, and thus should
     * be downloaded from the cloud for tests.
     */
    public static final String ENTRY_ID_REMOTE = "202b10fe-b028-4b84-9b84-852aa766607dx";


    /**
     * The remote test slave device. This can be modified using the @BeforeClass (e.g. by the
     * mock environment)
     */
    public static String REMOTE_SLAVE_SERVER = TestConstants.TEST_REMOTE_SLAVE_SERVER;

}
