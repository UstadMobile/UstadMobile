package com.ustadmobile.test.core.impl;

/**
 * Created by mike on 5/14/17.
 */

public class PlatformTestUtil {

    public static Object getTargetContext() {
        throw new RuntimeException("PlatformTestUtil sharedse: Must use implementation");
    }

    public static Object getTestContext() {
        throw new RuntimeException("PlatformTestUtil sharedse: Must use implementation");
    }

    public static String getRemoteTestEndpoint() {
        throw new RuntimeException("PlatformTestUtil sharedse: Must use implementation");
    }

}
