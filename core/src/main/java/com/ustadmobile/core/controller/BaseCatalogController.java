package com.ustadmobile.core.controller;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;

/**
 * Created by mike on 4/20/17.
 */

public abstract class BaseCatalogController extends UstadBaseController {

    public interface AcquisitionChoicesCompletedCallback {

        void onChoicesCompleted(UstadJSOPDSFeed preparedFeed);

    }


    public BaseCatalogController(Object context, boolean statusEventListeningEnabled) {
        super(context, statusEventListeningEnabled);
    }

    public BaseCatalogController(Object context) {
        super(context);
    }

    protected void showUserAcquisitionChoices(UstadJSOPDSFeed acquisitionEntries, AcquisitionChoicesCompletedCallback completedCallback) {

    }

}
