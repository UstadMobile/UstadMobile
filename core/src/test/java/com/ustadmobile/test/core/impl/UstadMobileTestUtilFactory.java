package com.ustadmobile.test.core.impl;

import com.ustadmobile.test.sharedse.impl.UstadMobileTestUtilSE;

/**
 * Created by mike on 4/27/17.
 */

public class UstadMobileTestUtilFactory {

    public static UstadMobileTestUtil makeTestUtil() {
        return new UstadMobileTestUtilCore();
    }

}
