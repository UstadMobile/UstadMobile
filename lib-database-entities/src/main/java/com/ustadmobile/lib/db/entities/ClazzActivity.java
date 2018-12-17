package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 11)
public class ClazzActivity {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long clazzActivityUid;

    //The activity change ClazzActivityChange
    private long clazzActivityClazzActivityChangeUid;

    //thumbs up or thumbs down
    private boolean clazzActivityGoodFeedback;

    //any notes
    private String clazzActivityNotes;

    //the date
    private long clazzActivityLogDate;

    //the clazz
    private long clazzActivityClazzUid;

    //is it done?
    private boolean clazzActivityDone;

    //the quantity of activity - from unit of measure (frequency, duration, binary)
    private long clazzActivityQuantity;

    @UmSyncMasterChangeSeqNum
    private long clazzActivityMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long clazzActivityLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int clazzActivityLastChangedBy;


    public long getClazzActivityMasterChangeSeqNum() {
        return clazzActivityMasterChangeSeqNum;
    }

    public void setClazzActivityMasterChangeSeqNum(long clazzActivityMasterChangeSeqNum) {
        this.clazzActivityMasterChangeSeqNum = clazzActivityMasterChangeSeqNum;
    }

    public long getClazzActivityLocalChangeSeqNum() {
        return clazzActivityLocalChangeSeqNum;
    }

    public void setClazzActivityLocalChangeSeqNum(long clazzActivityLocalChangeSeqNum) {
        this.clazzActivityLocalChangeSeqNum = clazzActivityLocalChangeSeqNum;
    }

    public long getClazzActivityQuantity() {
        return clazzActivityQuantity;
    }

    public void setClazzActivityQuantity(long clazzActivityQuantity) {
        this.clazzActivityQuantity = clazzActivityQuantity;
    }

    public boolean isClazzActivityDone() {
        return clazzActivityDone;
    }

    public void setClazzActivityDone(boolean clazzActivityDone) {
        this.clazzActivityDone = clazzActivityDone;
    }

    public long getClazzActivityClazzUid() {
        return clazzActivityClazzUid;
    }

    public void setClazzActivityClazzUid(long clazzActivityClazzUid) {
        this.clazzActivityClazzUid = clazzActivityClazzUid;
    }

    public long getClazzActivityUid() {
        return clazzActivityUid;
    }

    public void setClazzActivityUid(long clazzActivityUid) {
        this.clazzActivityUid = clazzActivityUid;
    }

    public long getClazzActivityClazzActivityChangeUid() {
        return clazzActivityClazzActivityChangeUid;
    }

    public void setClazzActivityClazzActivityChangeUid(long clazzActivityClazzActivityChangeUid) {
        this.clazzActivityClazzActivityChangeUid = clazzActivityClazzActivityChangeUid;
    }

    public boolean isClazzActivityGoodFeedback() {
        return clazzActivityGoodFeedback;
    }

    public void setClazzActivityGoodFeedback(boolean clazzActivityGoodFeedback) {
        this.clazzActivityGoodFeedback = clazzActivityGoodFeedback;
    }

    public String getClazzActivityNotes() {
        return clazzActivityNotes;
    }

    public void setClazzActivityNotes(String clazzActivityNotes) {
        this.clazzActivityNotes = clazzActivityNotes;
    }

    public long getClazzActivityLogDate() {
        return clazzActivityLogDate;
    }

    public void setClazzActivityLogDate(long clazzActivityLogDate) {
        this.clazzActivityLogDate = clazzActivityLogDate;
    }

    public int getClazzActivityLastChangedBy() {
        return clazzActivityLastChangedBy;
    }

    public void setClazzActivityLastChangedBy(int clazzActivityLastChangedBy) {
        this.clazzActivityLastChangedBy = clazzActivityLastChangedBy;
    }
}
