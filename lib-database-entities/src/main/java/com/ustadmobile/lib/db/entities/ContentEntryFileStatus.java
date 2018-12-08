package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentEntryFileStatus.TABLE_ID;


/**
 * This entity represents a downloaded ContentEntryFile. This entity is not synced, and represents
 * the status of a file on the local device.
 */
@UmEntity(tableId = TABLE_ID)
public class ContentEntryFileStatus {

    public static final int TABLE_ID = 12;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long cefsUid;

    private long cefsContentEntryFileUid;

    private String filePath;

    @UmSyncLocalChangeSeqNum
    private long cefsLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long cefsMasterChangeSeqNum;

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

    public long getCefsLocalChangeSeqNum() {
        return cefsLocalChangeSeqNum;
    }

    public void setCefsLocalChangeSeqNum(long cefsLocalChangeSeqNum) {
        this.cefsLocalChangeSeqNum = cefsLocalChangeSeqNum;
    }

    public long getCefsMasterChangeSeqNum() {
        return cefsMasterChangeSeqNum;
    }

    public void setCefsMasterChangeSeqNum(long cefsMasterChangeSeqNum) {
        this.cefsMasterChangeSeqNum = cefsMasterChangeSeqNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentEntryFileStatus that = (ContentEntryFileStatus) o;

        if (cefsUid != that.cefsUid) return false;
        if (cefsContentEntryFileUid != that.cefsContentEntryFileUid) return false;
        return filePath != null ? filePath.equals(that.filePath) : that.filePath == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (cefsUid ^ (cefsUid >>> 32));
        result = 31 * result + (int) (cefsContentEntryFileUid ^ (cefsContentEntryFileUid >>> 32));
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        return result;
    }
}
