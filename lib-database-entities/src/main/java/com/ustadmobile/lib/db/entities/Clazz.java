package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class Clazz implements SyncableEntity{

    @UmPrimaryKey(autoIncrement = true)
    private long clazzUid;

    private String clazzName;

    private String clazzDesc;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;

    private float attendanceAverage;

    //Gives the Holiday calendar Uid
    private long clazzHolidayUMCalendarUid;

    //Gives the schedule calendar ui
    private long clazzScheuleUMCalendarUid;

    //Active
    private boolean clazzActive;

    public boolean isClazzActive() {
        return clazzActive;
    }

    public void setClazzActive(boolean clazzActive) {
        this.clazzActive = clazzActive;
    }

    public String getClazzDesc() {
        return clazzDesc;
    }

    public void setClazzDesc(String clazzDesc) {
        this.clazzDesc = clazzDesc;
    }

    public long getClazzHolidayUMCalendarUid() {
        return clazzHolidayUMCalendarUid;
    }

    public void setClazzHolidayUMCalendarUid(long clazzHolidayUMCalendarUid) {
        this.clazzHolidayUMCalendarUid = clazzHolidayUMCalendarUid;
    }

    public long getClazzScheuleUMCalendarUid() {
        return clazzScheuleUMCalendarUid;
    }

    public void setClazzScheuleUMCalendarUid(long clazzScheuleUMCalendarUid) {
        this.clazzScheuleUMCalendarUid = clazzScheuleUMCalendarUid;
    }

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
}
