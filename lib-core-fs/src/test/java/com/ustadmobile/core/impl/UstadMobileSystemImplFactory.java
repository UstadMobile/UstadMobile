package com.ustadmobile.core.impl;

import com.ustadmobile.port.javase.impl.UstadMobileSystemImplJavaSe;

/**
 * Created by mike on 4/25/17.
 */
@SuppressWarnings("Duplicates")
public class UstadMobileSystemImplFactory {

    /**
     * @return
     */
    public static Object makeSystemImpl() {
        return new UstadMobileSystemImplJavaSe();
    }
}
