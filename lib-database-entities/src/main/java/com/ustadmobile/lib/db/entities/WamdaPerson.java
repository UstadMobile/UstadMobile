package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 1005)
public class WamdaPerson {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long wamdaPersonUid;

    private long wamdaPersonPersonUid;

    private int pointScore;

    private String profileStatus;

    private String profileImage;

    @UmSyncLocalChangeSeqNum
    private long wamdaPersonLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long wamdaPersonMasterChangeSeqNum;

    public long getWamdaPersonUid() {
        return wamdaPersonUid;
    }

    public void setWamdaPersonUid(long wamdaPersonUid) {
        this.wamdaPersonUid = wamdaPersonUid;
    }

    public long getWamdaPersonPersonUid() {
        return wamdaPersonPersonUid;
    }

    public void setWamdaPersonPersonUid(long wamdaPersonPersonUid) {
        this.wamdaPersonPersonUid = wamdaPersonPersonUid;
    }

    public int getPointScore() {
        return pointScore;
    }

    public void setPointScore(int pointScore) {
        this.pointScore = pointScore;
    }

    public String getProfileStatus() {
        return profileStatus;
    }

    public void setProfileStatus(String profileStatus) {
        this.profileStatus = profileStatus;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getWamdaPersonLocalChangeSeqNum() {
        return wamdaPersonLocalChangeSeqNum;
    }

    public void setWamdaPersonLocalChangeSeqNum(long wamdaPersonLocalChangeSeqNum) {
        this.wamdaPersonLocalChangeSeqNum = wamdaPersonLocalChangeSeqNum;
    }

    public long getWamdaPersonMasterChangeSeqNum() {
        return wamdaPersonMasterChangeSeqNum;
    }

    public void setWamdaPersonMasterChangeSeqNum(long wamdaPersonMasterChangeSeqNum) {
        this.wamdaPersonMasterChangeSeqNum = wamdaPersonMasterChangeSeqNum;
    }
}
