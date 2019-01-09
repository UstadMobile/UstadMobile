package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 43)
public class ExampleSyncableEntityWithAttachment {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long eseUid;

    @UmSyncLocalChangeSeqNum
    private long eseLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long eseMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int eseLastChangedBy;

    private String filename;

    private String mimeType;

    public long getEseUid() {
        return eseUid;
    }

    public void setEseUid(long eseUid) {
        this.eseUid = eseUid;
    }

    public long getEseLocalChangeSeqNum() {
        return eseLocalChangeSeqNum;
    }

    public void setEseLocalChangeSeqNum(long eseLocalChangeSeqNum) {
        this.eseLocalChangeSeqNum = eseLocalChangeSeqNum;
    }

    public long getEseMasterChangeSeqNum() {
        return eseMasterChangeSeqNum;
    }

    public void setEseMasterChangeSeqNum(long eseMasterChangeSeqNum) {
        this.eseMasterChangeSeqNum = eseMasterChangeSeqNum;
    }

    public int getEseLastChangedBy() {
        return eseLastChangedBy;
    }

    public void setEseLastChangedBy(int eseLastChangedBy) {
        this.eseLastChangedBy = eseLastChangedBy;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
