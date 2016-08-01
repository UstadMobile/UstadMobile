package com.ustadmobile.port.android.impl;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.UstadMobileSystemImplFactory;

/**
 * Created by mike on 8/1/16.
 */
public class UstadMobileSystemImplFactoryAndroid extends UstadMobileSystemImplFactory{

    @Override
    public UstadMobileSystemImpl makeUstadSystemImpl() {
        return new UstadMobileSystemImplAndroid();
    }
}
