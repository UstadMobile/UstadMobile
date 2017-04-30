package com.ustadmobile.core.impl;

import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

/**
 * Created by mike on 4/25/17.
 */
@SuppressWarnings("Duplicates")
public class UstadMobileSystemImplFactory {

    /**
     * @return
     */
    public static UstadMobileSystemImpl makeSystemImpl() {
        return new UstadMobileSystemImplAndroid();
    }

}
