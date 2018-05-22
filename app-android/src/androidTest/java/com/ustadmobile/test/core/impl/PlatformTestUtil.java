package com.ustadmobile.test.core.impl;


import android.support.test.InstrumentationRegistry;

import static com.ustadmobile.test.core.buildconfig.TestConstants.TEST_REMOTE_SLAVE_SERVER;
import static com.ustadmobile.test.core.buildconfig.TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT;


/**
 * TestUtil is designed to abstract away the differences between conducting testing on "smart"
 * devices where we can run NanoHTTPD and J2ME where we need to use an external server.
 */

public class PlatformTestUtil {


    public static Object getTargetContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    public static Object getTestContext() {
        return InstrumentationRegistry.getContext();
    }

    public static String getRemoteTestEndpoint() {
        return "http://" + TEST_REMOTE_SLAVE_SERVER + ":" + TEST_REMOTE_SLAVE_SERVER_PORT + "/";
    }

}
