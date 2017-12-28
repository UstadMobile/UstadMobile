package com.ustadmobile.test.core.impl;

/**
 * Created by mike on 12/26/17.
 */

public class PlatformTestUtil {

    static TestContext testContext = new TestContext("maindevice");

    public static Object getTargetContext() {
        return testContext;
    }

    public static Object getTestContext() {
        return new Object();
    }

}
