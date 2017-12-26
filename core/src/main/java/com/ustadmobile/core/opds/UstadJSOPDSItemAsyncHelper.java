package com.ustadmobile.core.opds;

/**
 * Handles async loading of UstadJSOPDSItem. This is normally done using threading, but is separated
 * out so that it can overriden on GWT (which does not support threads).
 *
 */
class UstadJSOPDSItemAsyncHelper {

    private UstadJSOPDSItem item;

    UstadJSOPDSItemAsyncHelper(UstadJSOPDSItem item) {
        this.item = item;
    }

    void start() {
        new Thread(item).start();
    }

}
