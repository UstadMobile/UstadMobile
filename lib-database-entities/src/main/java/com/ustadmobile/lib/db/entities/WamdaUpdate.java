package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 1009)
public class WamdaUpdate {

    @UmPrimaryKey (autoGenerateSyncable = true)
    private long wamdUpdateUid;

    private long wamdaUpdatePersonUid;

    private String wamdaUpdateDesitination;

    private String wamdaUpdateText;

    private long timestamp;

    @UmSyncLocalChangeSeqNum
    private long wamdaUpdateLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long wamdaUpdateMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int wamdaUpdateLastChangeBy;

    public long getWamdUpdateUid() {
        return wamdUpdateUid;
    }

    public void setWamdUpdateUid(long wamdUpdateUid) {
        this.wamdUpdateUid = wamdUpdateUid;
    }

    public long getWamdaUpdatePersonUid() {
        return wamdaUpdatePersonUid;
    }

    public void setWamdaUpdatePersonUid(long wamdaUpdatePersonUid) {
        this.wamdaUpdatePersonUid = wamdaUpdatePersonUid;
    }

    public String getWamdaUpdateDesitination() {
        return wamdaUpdateDesitination;
    }

    public void setWamdaUpdateDesitination(String wamdaUpdateDesitination) {
        this.wamdaUpdateDesitination = wamdaUpdateDesitination;
    }

    public String getWamdaUpdateText() {
        return wamdaUpdateText;
    }

    public void setWamdaUpdateText(String wamdaUpdateText) {
        this.wamdaUpdateText = wamdaUpdateText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getWamdaUpdateLocalChangeSeqNum() {
        return wamdaUpdateLocalChangeSeqNum;
    }

    public void setWamdaUpdateLocalChangeSeqNum(long wamdaUpdateLocalChangeSeqNum) {
        this.wamdaUpdateLocalChangeSeqNum = wamdaUpdateLocalChangeSeqNum;
    }

    public long getWamdaUpdateMasterChangeSeqNum() {
        return wamdaUpdateMasterChangeSeqNum;
    }

    public void setWamdaUpdateMasterChangeSeqNum(long wamdaUpdateMasterChangeSeqNum) {
        this.wamdaUpdateMasterChangeSeqNum = wamdaUpdateMasterChangeSeqNum;
    }

    public int getWamdaUpdateLastChangeBy() {
        return wamdaUpdateLastChangeBy;
    }

    public void setWamdaUpdateLastChangeBy(int wamdaUpdateLastChangeBy) {
        this.wamdaUpdateLastChangeBy = wamdaUpdateLastChangeBy;
    }
}
