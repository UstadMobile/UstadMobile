package com.ustadmobile.test.core.impl;


/**
 * TestUtil is designed to abstract away the differences between conducting testing on "smart"
 * devices where we can run NanoHTTPD and J2ME where we need to use an external server.
 */

public class PlatformTestUtil {

    public static Object getTargetContext() {
        return new Object();
    }

    public static Object getTestContext() {
        return new Object();
    }


}
