package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 1004)
public class WamdaLike {

    @UmPrimaryKey (autoGenerateSyncable =  true)
    private long wamdaLikeUid;

    private long wamdaLikePersonUid;

    private long wamdaLikeClazzUid;

    private long wamdaLikeDiscussionUid;

    private long timeStamp;

    @UmSyncLocalChangeSeqNum
    private long wamdaLikeLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long wamdaLikeMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int wamdaLikeLastChangedBy;

    public long getWamdaLikeUid() {
        return wamdaLikeUid;
    }

    public void setWamdaLikeUid(long wamdaLikeUid) {
        this.wamdaLikeUid = wamdaLikeUid;
    }

    public long getWamdaLikePersonUid() {
        return wamdaLikePersonUid;
    }

    public void setWamdaLikePersonUid(long wamdaLikePersonUid) {
        this.wamdaLikePersonUid = wamdaLikePersonUid;
    }

    public long getWamdaLikeClazzUid() {
        return wamdaLikeClazzUid;
    }

    public void setWamdaLikeClazzUid(long wamdaLikeClazzUid) {
        this.wamdaLikeClazzUid = wamdaLikeClazzUid;
    }

    public long getWamdaLikeDiscussionUid() {
        return wamdaLikeDiscussionUid;
    }

    public void setWamdaLikeDiscussionUid(long wamdaLikeDiscussionUid) {
        this.wamdaLikeDiscussionUid = wamdaLikeDiscussionUid;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getWamdaLikeLocalChangeSeqNum() {
        return wamdaLikeLocalChangeSeqNum;
    }

    public void setWamdaLikeLocalChangeSeqNum(long wamdaLikeLocalChangeSeqNum) {
        this.wamdaLikeLocalChangeSeqNum = wamdaLikeLocalChangeSeqNum;
    }

    public long getWamdaLikeMasterChangeSeqNum() {
        return wamdaLikeMasterChangeSeqNum;
    }

    public void setWamdaLikeMasterChangeSeqNum(long wamdaLikeMasterChangeSeqNum) {
        this.wamdaLikeMasterChangeSeqNum = wamdaLikeMasterChangeSeqNum;
    }

    public int getWamdaLikeLastChangedBy() {
        return wamdaLikeLastChangedBy;
    }

    public void setWamdaLikeLastChangedBy(int wamdaLikeLastChangedBy) {
        this.wamdaLikeLastChangedBy = wamdaLikeLastChangedBy;
    }
}
