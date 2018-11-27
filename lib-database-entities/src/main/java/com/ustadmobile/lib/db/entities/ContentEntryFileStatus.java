package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * This entity represents a downloaded ContentEntryFile. This entity is not synced, and represents
 * the status of a file on the local device.
 */
@UmEntity
public class ContentEntryFileStatus {

    @UmPrimaryKey(autoIncrement = true)
    private long cefsUid;

    private long cefsContentEntryFileUid;

    private String filePath;

    public long getCefsUid() {
        return cefsUid;
    }

    public void setCefsUid(long cefsUid) {
        this.cefsUid = cefsUid;
    }

    public long getCefsContentEntryFileUid() {
        return cefsContentEntryFileUid;
    }

    public void setCefsContentEntryFileUid(long cefsContentEntryFileUid) {
        this.cefsContentEntryFileUid = cefsContentEntryFileUid;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
