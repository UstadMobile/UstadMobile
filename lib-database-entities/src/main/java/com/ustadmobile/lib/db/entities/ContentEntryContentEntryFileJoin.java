package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Join Entry to link a ContentEntry to a ContentEntryFile that actually contains the content. That
 * file can then be downloaded using an endpoint.
 */
//short code cecefj
@UmEntity
public class ContentEntryContentEntryFileJoin {

    @UmPrimaryKey(autoIncrement = true)
    private long cecefjUid;

    private long cecefjContentEntryUid;

    private long cecefjContentEntryFileUid;

    public long getCecefjUid() {
        return cecefjUid;
    }

    public void setCecefjUid(long cecefjUid) {
        this.cecefjUid = cecefjUid;
    }

    public long getCecefjContentEntryUid() {
        return cecefjContentEntryUid;
    }

    public void setCecefjContentEntryUid(long cecefjContentEntryUid) {
        this.cecefjContentEntryUid = cecefjContentEntryUid;
    }

    public long getCecefjContentEntryFileUid() {
        return cecefjContentEntryFileUid;
    }

    public void setCecefjContentEntryFileUid(long cecefjContentEntryFileUid) {
        this.cecefjContentEntryFileUid = cecefjContentEntryFileUid;
    }
}
