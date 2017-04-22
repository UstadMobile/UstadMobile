package com.ustadmobile.core.impl;

/**
 * Class that listens to acquisition status.
 *
 * Created by mike on 4/19/17.
 */

public interface AcquisitionStatusListener {

    /**
     * The status of an acquisition has changed
     *
     * @param event The acquisition status event
     */
    void statusUpdated(AcquisitionStatusEvent event);

}
