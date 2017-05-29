package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Created by mike on 5/29/17.
 */

public interface AcquisitionListener {

    void acquisitionProgressUpdate(String entryId, AcquisitionTask.Status status);

    void acquisitionStatusChanged(String entryId, AcquisitionTask.Status status);
}
