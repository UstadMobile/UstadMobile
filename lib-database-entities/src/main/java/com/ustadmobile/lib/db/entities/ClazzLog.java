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

    private long clazzClazzUid;

    private long logDate;

    private long timeRecorded;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;

    private boolean done;

    private int numPresent;

    private int numAbsent;

    private int numPartial;

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

    public long getClazzClazzUid() {
        return clazzClazzUid;
    }

    public void setClazzClazzUid(long clazzClazzUid) {
        this.clazzClazzUid = clazzClazzUid;
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
