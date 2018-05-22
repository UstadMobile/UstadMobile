package com.ustadmobile.core.networkmanager;

/**
 * Created by mike on 5/29/17.
 */
public interface AcquisitionListener {

    void acquisitionProgressUpdate(String entryId, AcquisitionTaskStatus status);

    void acquisitionStatusChanged(String entryId, AcquisitionTaskStatus status);
}
