package com.ustadmobile.test.core.impl;


import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.test.port.sharedse.impl.UstadMobileSystemImplTestSE;


/**
 * TestUtil is designed to abstract away the differences between conducting testing on "smart"
 * devices where we can run NanoHTTPD and J2ME where we need to use an external server.
 */

public class PlatformTestUtil {


    static TestContext testContext = new TestContext("maindevice");

    public static Object getTargetContext() {
        return testContext;
    }

    public static Object getTestContext() {
        return new Object();
    }

    public static String getRemoteTestEndpoint() {
        UstadMobileSystemImplTestSE implTestSE = (UstadMobileSystemImplTestSE)UstadMobileSystemImpl.getInstance();
        return "http://localhost:" + implTestSE.getMockTestDriver().getTestControlServerPort() + "/";
    }
}
