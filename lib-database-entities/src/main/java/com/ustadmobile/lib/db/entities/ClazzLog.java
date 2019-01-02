package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents one session (e.g. day) in the class log book. This is related to attendance records, but
 * could also be related to behavior logs etc. in the future.
 */
@UmEntity(tableId = 14)
public class ClazzLog{

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long clazzLogUid;

    private long clazzLogClazzUid;

    private long logDate;

    private long timeRecorded;

    private boolean done;

    private int numPresent;

    private int numAbsent;

    private int numPartial;

    @UmSyncMasterChangeSeqNum
    private long clazzLogChangeMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long clazzLogChangeLocalChangeSeqNum;

    public int getClazzLogLastChangedBy() {
        return clazzLogLastChangedBy;
    }

    public void setClazzLogLastChangedBy(int clazzLogLastChangedBy) {
        this.clazzLogLastChangedBy = clazzLogLastChangedBy;
    }

    @UmSyncLastChangedBy
    private int clazzLogLastChangedBy;

    /**
     * Gets the number of members passed
     *
     * @return
     */
    public int getNumPresent() {
        return numPresent;
    }

    /**
     * Sets the number of members present
     *
     * @param numPresent    number of present members
     */
    public void setNumPresent(int numPresent) {
        this.numPresent = numPresent;
    }

    /**
     * Gets the number of members absent
     * @return
     */
    public int getNumAbsent() {
        return numAbsent;
    }

    /**
     * Sets the number of members absent
     * @param numAbsent
     */
    public void setNumAbsent(int numAbsent) {
        this.numAbsent = numAbsent;
    }

    /**
     * Gets the number of members partially present
     *
     * @return
     */
    public int getNumPartial() {
        return numPartial;
    }

    /**
     * Sets the number of members partially present
     * @param numPartial
     */
    public void setNumPartial(int numPartial) {
        this.numPartial = numPartial;
    }

    /**
     * Gets if Log is done.
     * @return if log entry is done.
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Sets log entry done status
     * @param done the done status
     */
    public void setDone(boolean done) {
        this.done = done;
    }

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


    public long getClazzLogChangeMasterChangeSeqNum() {
        return clazzLogChangeMasterChangeSeqNum;
    }

    public void setClazzLogChangeMasterChangeSeqNum(long clazzLogChangeMasterChangeSeqNum) {
        this.clazzLogChangeMasterChangeSeqNum = clazzLogChangeMasterChangeSeqNum;
    }

    public long getClazzLogChangeLocalChangeSeqNum() {
        return clazzLogChangeLocalChangeSeqNum;
    }

    public void setClazzLogChangeLocalChangeSeqNum(long clazzLogChangeLocalChangeSeqNum) {
        this.clazzLogChangeLocalChangeSeqNum = clazzLogChangeLocalChangeSeqNum;
    }
}
