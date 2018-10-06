package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class WamdaLike {

    @UmPrimaryKey (autoIncrement =  true)
    private long wamdaLikeUid;

    private long wamdaLikePersonUid;

    private long wamdaLikeClazzUid;

    private long wamdaLikeDiscussionUid;

    private long timeStamp;

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
}
