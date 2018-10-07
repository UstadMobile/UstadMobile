package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class WamdaFollower {

    @UmPrimaryKey(autoIncrement = true)
    private long wamdaFollowerUid;

    private long wamdaFollowerPersonUid;

    private long timeStamp;

    private long wamdaFollowingPersonUid;

    public long getWamdaFollowerUid() {
        return wamdaFollowerUid;
    }

    public void setWamdaFollowerUid(long wamdaFollowerUid) {
        this.wamdaFollowerUid = wamdaFollowerUid;
    }

    public long getWamdaFollowerPersonUid() {
        return wamdaFollowerPersonUid;
    }

    public void setWamdaFollowerPersonUid(long wamdaFollowerPersonUid) {
        this.wamdaFollowerPersonUid = wamdaFollowerPersonUid;
    }

    public long getWamdaFollowingPersonUid() {
        return wamdaFollowingPersonUid;
    }

    public void setWamdaFollowingPersonUid(long wamdaFollowingPersonUid) {
        this.wamdaFollowingPersonUid = wamdaFollowingPersonUid;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WamdaFollower)) return false;

        WamdaFollower follower = (WamdaFollower) object;

        if (wamdaFollowerUid != follower.wamdaFollowerUid) return false;
        if (wamdaFollowerPersonUid != follower.wamdaFollowerPersonUid) return false;
        if (timeStamp != follower.timeStamp) return false;
        return wamdaFollowingPersonUid == follower.wamdaFollowingPersonUid;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (wamdaFollowerUid ^ (wamdaFollowerUid >>> 32));
        result = 31 * result + (int) (wamdaFollowerPersonUid ^ (wamdaFollowerPersonUid >>> 32));
        result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
        result = 31 * result + (int) (wamdaFollowingPersonUid ^ (wamdaFollowingPersonUid >>> 32));
        return result;
    }
}
