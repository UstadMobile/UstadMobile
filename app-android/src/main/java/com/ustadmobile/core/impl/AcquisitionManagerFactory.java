package com.ustadmobile.core.impl;

import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.port.android.impl.AcquisitionManagerAndroid;

/**
 * Created by mike on 4/19/17.
 */

public class AcquisitionManagerFactory {

    public static AcquisitionManager makeAcquisitionManager() {
        return new AcquisitionManagerAndroid();
    }

}
