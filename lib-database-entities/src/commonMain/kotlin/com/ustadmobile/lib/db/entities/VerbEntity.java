package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.VerbEntity.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class VerbEntity {

    public static final int TABLE_ID = 62;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long verbUid;

    private String urlId;

    @UmSyncMasterChangeSeqNum
    private long verbMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long verbLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int verbLastChangedBy;

    public long getVerbUid() {
        return verbUid;
    }

    public void setVerbUid(long verbUid) {
        this.verbUid = verbUid;
    }

    public String getUrlId() {
        return urlId;
    }

    public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

    public long getVerbMasterChangeSeqNum() {
        return verbMasterChangeSeqNum;
    }

    public void setVerbMasterChangeSeqNum(long verbMasterChangeSeqNum) {
        this.verbMasterChangeSeqNum = verbMasterChangeSeqNum;
    }

    public long getVerbLocalChangeSeqNum() {
        return verbLocalChangeSeqNum;
    }

    public void setVerbLocalChangeSeqNum(long verbLocalChangeSeqNum) {
        this.verbLocalChangeSeqNum = verbLocalChangeSeqNum;
    }

    public int getVerbLastChangedBy() {
        return verbLastChangedBy;
    }

    public void setVerbLastChangedBy(int verbLastChangedBy) {
        this.verbLastChangedBy = verbLastChangedBy;
    }
}
