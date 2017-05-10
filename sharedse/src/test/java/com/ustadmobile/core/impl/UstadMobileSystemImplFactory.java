package com.ustadmobile.core.impl;

import com.ustadmobile.test.sharedse.impl.UstadMobileSystemImplTest;

/**
 * Created by mike on 4/25/17.
 */
@SuppressWarnings("Duplicates")
public class UstadMobileSystemImplFactory {

    /**
     * @return
     */
    public static UstadMobileSystemImpl makeSystemImpl() {
        return new UstadMobileSystemImplTest();
    }
}
