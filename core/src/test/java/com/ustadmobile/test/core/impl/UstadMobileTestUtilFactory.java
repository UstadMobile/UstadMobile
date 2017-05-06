package com.ustadmobile.test.core.impl;

import com.ustadmobile.test.core.impl.se.UstadMobileTestUtilSE;

/**
 * Created by mike on 4/27/17.
 */

public class UstadMobileTestUtilFactory {

    public static UstadMobileTestUtil makeTestUtil() {
        return new UstadMobileTestUtilSE();
    }

}
