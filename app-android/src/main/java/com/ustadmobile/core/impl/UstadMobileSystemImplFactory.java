package com.ustadmobile.core.impl;

/**
 * Created by mike on 4/25/17.
 */
@SuppressWarnings("Duplicates")
public class UstadMobileSystemImplFactory {

    /**
     * @return
     */
    public static UstadMobileSystemImpl makeSystemImpl() {
        throw new RuntimeException("The base system impl factory must be overriden");
    }

}
