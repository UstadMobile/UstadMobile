package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;

/**
 * Created by mike on 5/29/17.
 */
public interface AcquisitionListener {

    void acquisitionProgressUpdate(String entryId, AcquisitionTaskStatus status);

    void acquisitionStatusChanged(String entryId, AcquisitionTaskStatus status);
}
