package com.ustadmobile.test.core.impl;


import com.ustadmobile.port.javase.impl.UmContextSe;

/**
 * TestUtil is designed to abstract away the differences between conducting testing on "smart"
 * devices where we can run NanoHTTPD and J2ME where we need to use an external server.
 */

public class PlatformTestUtil {

    static UmContextSe testContext = new UmContextSe();

    public static Object getTargetContext() {
        return testContext;
    }

    public static Object getTestContext() {
        return new Object();
    }

}
