package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 6)
public class Clazz implements SyncableEntity{

    @UmPrimaryKey(autoIncrement = true)
    private long clazzUid;

    private String clazzName;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;

    private float attendanceAverage;

    @UmSyncMasterChangeSeqNum
    private long clazzMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long clazzLocalChangeSeqNum;

    public float getAttendanceAverage() {
        return attendanceAverage;
    }

    public void setAttendanceAverage(float attendanceAverage) {
        this.attendanceAverage = attendanceAverage;
    }

    public long getClazzUid() {
        return clazzUid;
    }

    public void setClazzUid(long clazzUid) {
        this.clazzUid = clazzUid;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    @Override
    public long getMasterChangeSeqNum() {
        return masterChangeSeqNum;
    }

    @Override
    public void setMasterChangeSeqNum(long masterChangeSeqNum) {
        this.masterChangeSeqNum = masterChangeSeqNum;
    }

    @Override
    public long getLocalChangeSeqNum() {
        return localChangeSeqNum;
    }

    @Override
    public void setLocalChangeSeqNum(long localChangeSeqNum) {
        this.localChangeSeqNum = localChangeSeqNum;
    }

    public long getClazzMasterChangeSeqNum() {
        return clazzMasterChangeSeqNum;
    }

    public void setClazzMasterChangeSeqNum(long clazzMasterChangeSeqNum) {
        this.clazzMasterChangeSeqNum = clazzMasterChangeSeqNum;
    }

    public long getClazzLocalChangeSeqNum() {
        return clazzLocalChangeSeqNum;
    }

    public void setClazzLocalChangeSeqNum(long clazzLocalChangeSeqNum) {
        this.clazzLocalChangeSeqNum = clazzLocalChangeSeqNum;
    }
}
