package com.ustadmobile.core.impl;

/**
 * Created by mike on 4/19/17.
 */

public class AcquisitionManagerFactory {

    public static AcquisitionManager makeAcquisitionManager() {
        throw new RuntimeException("Acquisition manager runtime implementation must override core impl");
    }

}
