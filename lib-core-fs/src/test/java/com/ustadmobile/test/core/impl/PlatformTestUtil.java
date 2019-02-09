package com.ustadmobile.test.core.impl;

import com.ustadmobile.core.fs.buildconfig.TestConstantsCoreFs;
import com.ustadmobile.port.javase.impl.UmContextSe;

/**
 * Created by mike on 12/26/17.
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
