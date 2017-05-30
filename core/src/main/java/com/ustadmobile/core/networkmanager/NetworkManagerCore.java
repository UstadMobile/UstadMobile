package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;

/**
 * Created by kileha3 on 13/02/2017.
 */

public interface NetworkManagerCore {

    void setSuperNodeEnabled(Object context,boolean enabled);

    UstadJSOPDSFeed requestAcquisition(UstadJSOPDSFeed feed, Object mContext, boolean localNetworkEnabled, boolean wifiDirectEnabled);

    void addAcquisitionTaskListener(AcquisitionListener listener);

    void removeAcquisitionTaskListener(AcquisitionListener listener);

}
