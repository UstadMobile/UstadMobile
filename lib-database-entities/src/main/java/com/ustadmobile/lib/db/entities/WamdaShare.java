package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class WamdaShare {

    @UmPrimaryKey (autoIncrement =  true)
    private long wamdaShareUid;

    private long wamdaSharePersonUid;

    private long wamdaShareClazzUid;

    private long wamdaShareDiscussionUid;

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
}
