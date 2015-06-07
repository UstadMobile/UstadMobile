package com.ustadmobile.impl;

/**
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplFactory {

    public static UstadMobileSystemImpl createUstadSystemImpl() {
        return new UstadMobileSystemImplAndroid();
    }
}
