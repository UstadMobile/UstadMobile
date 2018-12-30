package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;


import static com.ustadmobile.lib.db.entities.Clazz.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class Clazz {

    public static final int TABLE_ID = 6;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long clazzUid;

    private String clazzName;

    private String clazzDesc;

    private float attendanceAverage;

    //Gives the Holiday calendar Uid
    private long clazzHolidayUMCalendarUid;

    //Gives the schedule calendar ui
    private long clazzScheuleUMCalendarUid;

    //Active
    private boolean clazzActive;

    @UmSyncMasterChangeSeqNum
    private long clazzMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long clazzLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int clazzLastChangedBy;

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

    private long clazzLocationUid;

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

    public int getClazzLastChangedBy() {
        return clazzLastChangedBy;
    }

    public void setClazzLastChangedBy(int clazzLastChangedBy) {
        this.clazzLastChangedBy = clazzLastChangedBy;
    }

    public long getClazzLocationUid() {
        return clazzLocationUid;
    }

    public void setClazzLocationUid(long clazzLocationUid) {
        this.clazzLocationUid = clazzLocationUid;
    }
}
