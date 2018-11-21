package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 1007)
public class WamdaShare {

    @UmPrimaryKey (autoGenerateSyncable=  true)
    private long wamdaShareUid;

    private long wamdaSharePersonUid;

    private long wamdaShareClazzUid;

    private long wamdaShareDiscussionUid;

    @UmSyncLocalChangeSeqNum
    private long localChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long masterChangeSeqNum;

    public long getWamdaShareUid() {
        return wamdaShareUid;
    }

    public void setWamdaShareUid(long wamdaShareUid) {
        this.wamdaShareUid = wamdaShareUid;
    }

    public long getWamdaSharePersonUid() {
        return wamdaSharePersonUid;
    }

    public void setWamdaSharePersonUid(long wamdaSharePersonUid) {
        this.wamdaSharePersonUid = wamdaSharePersonUid;
    }

    public long getWamdaShareClazzUid() {
        return wamdaShareClazzUid;
    }

    public void setWamdaShareClazzUid(long wamdaShareClazzUid) {
        this.wamdaShareClazzUid = wamdaShareClazzUid;
    }

    public long getWamdaShareDiscussionUid() {
        return wamdaShareDiscussionUid;
    }

    public void setWamdaShareDiscussionUid(long wamdaShareDiscussionUid) {
        this.wamdaShareDiscussionUid = wamdaShareDiscussionUid;
    }

    public long getLocalChangeSeqNum() {
        return localChangeSeqNum;
    }

    public void setLocalChangeSeqNum(long localChangeSeqNum) {
        this.localChangeSeqNum = localChangeSeqNum;
    }

    public long getMasterChangeSeqNum() {
        return masterChangeSeqNum;
    }

    public void setMasterChangeSeqNum(long masterChangeSeqNum) {
        this.masterChangeSeqNum = masterChangeSeqNum;
    }
}
