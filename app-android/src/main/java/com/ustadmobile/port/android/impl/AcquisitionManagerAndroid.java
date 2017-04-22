package com.ustadmobile.port.android.impl;

import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.AcquisitionStatusListener;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;

/**
 * Created by mike on 4/19/17.
 */

public class AcquisitionManagerAndroid extends AcquisitionManager {

    @Override
    public void acquireCatalogEntries(UstadJSOPDSFeed acquireFeed, Object context) {

    }

    @Override
    public int[] getEntryStatusById(String entryId, Object context) {
        return new int[0];
    }

    @Override
    public void registerEntryAquisitionStatusListener(AcquisitionStatusListener listener) {

    }

    @Override
    public void unregisterEntryAquisitionStatusListener(AcquisitionStatusListener listener) {

    }
}
