package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class WamdaFollower {

    @UmPrimaryKey(autoIncrement = true)
    private long wamdaFollowerUid;

    private long wamdaFollowerFollowerPersonUid;

    private long wamdaFollowerFollowingPersonUid;

    public long getWamdaFollowerUid() {
        return wamdaFollowerUid;
    }

    public void setWamdaFollowerUid(long wamdaFollowerUid) {
        this.wamdaFollowerUid = wamdaFollowerUid;
    }

    public long getWamdaFollowerFollowerPersonUid() {
        return wamdaFollowerFollowerPersonUid;
    }

    public void setWamdaFollowerFollowerPersonUid(long wamdaFollowerFollowerPersonUid) {
        this.wamdaFollowerFollowerPersonUid = wamdaFollowerFollowerPersonUid;
    }

    public long getWamdaFollowerFollowingPersonUid() {
        return wamdaFollowerFollowingPersonUid;
    }

    public void setWamdaFollowerFollowingPersonUid(long wamdaFollowerFollowingPersonUid) {
        this.wamdaFollowerFollowingPersonUid = wamdaFollowerFollowingPersonUid;
    }
}
