package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents one session (e.g. day) in the class log book. This is related to attendance records, but
 * could also be related to behavior logs etc. in the future.
 */
@UmEntity
public class ClazzLog implements SyncableEntity{

    @UmPrimaryKey(autoIncrement = true)
    private long clazzLogUid;

    private long clazzLogClazzUid;

    private long logDate;

    private long timeRecorded;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;

    public long getClazzLogUid() {
        return clazzLogUid;
    }

    public void setClazzLogUid(long clazzLogUid) {
        this.clazzLogUid = clazzLogUid;
    }

    public long getClazzLogClazzUid() {
        return clazzLogClazzUid;
    }

    public void setClazzLogClazzUid(long clazzLogClazzUid) {
        this.clazzLogClazzUid = clazzLogClazzUid;
    }

    public long getLogDate() {
        return logDate;
    }

    public void setLogDate(long logDate) {
        this.logDate = logDate;
    }

    public long getTimeRecorded() {
        return timeRecorded;
    }

    public void setTimeRecorded(long timeRecorded) {
        this.timeRecorded = timeRecorded;
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
}
