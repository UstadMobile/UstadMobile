package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 1002)
public class WamdaClazz {

    @UmPrimaryKey (autoGenerateSyncable = true)
    private long wamdaClazzUid;

    private long wamdaClazzPersonUid;

    private long wamdaClazzClazzUid;

    private long wamdaClazzCreationTime;

    private String wamdaClazzDescription;

    private String wamdaClazzSubtitle;

    @UmSyncLocalChangeSeqNum
    private long wamdaClazzLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long wamdaClazzMasterChangeSeqNum;


    public long getWamdaClazzUid() {
        return wamdaClazzUid;
    }

    public void setWamdaClazzUid(long wamdaClazzUid) {
        this.wamdaClazzUid = wamdaClazzUid;
    }

    public long getWamdaClazzPersonUid() {
        return wamdaClazzPersonUid;
    }

    public void setWamdaClazzPersonUid(long wamdaClazzPersonUid) {
        this.wamdaClazzPersonUid = wamdaClazzPersonUid;
    }

    public long getWamdaClazzClazzUid() {
        return wamdaClazzClazzUid;
    }

    public void setWamdaClazzClazzUid(long wamdaClazzClazzUid) {
        this.wamdaClazzClazzUid = wamdaClazzClazzUid;
    }

    public long getWamdaClazzCreationTime() {
        return wamdaClazzCreationTime;
    }

    public void setWamdaClazzCreationTime(long wamdaClazzCreationTime) {
        this.wamdaClazzCreationTime = wamdaClazzCreationTime;
    }

    public String getWamdaClazzDescription() {
        return wamdaClazzDescription;
    }

    public void setWamdaClazzDescription(String wamdaClazzDescription) {
        this.wamdaClazzDescription = wamdaClazzDescription;
    }

    public String getWamdaClazzSubtitle() {
        return wamdaClazzSubtitle;
    }

    public void setWamdaClazzSubtitle(String wamdaClazzSubtitle) {
        this.wamdaClazzSubtitle = wamdaClazzSubtitle;
    }

    public long getWamdaClazzLocalChangeSeqNum() {
        return wamdaClazzLocalChangeSeqNum;
    }

    public void setWamdaClazzLocalChangeSeqNum(long wamdaClazzLocalChangeSeqNum) {
        this.wamdaClazzLocalChangeSeqNum = wamdaClazzLocalChangeSeqNum;
    }

    public long getWamdaClazzMasterChangeSeqNum() {
        return wamdaClazzMasterChangeSeqNum;
    }

    public void setWamdaClazzMasterChangeSeqNum(long wamdaClazzMasterChangeSeqNum) {
        this.wamdaClazzMasterChangeSeqNum = wamdaClazzMasterChangeSeqNum;
    }
}
