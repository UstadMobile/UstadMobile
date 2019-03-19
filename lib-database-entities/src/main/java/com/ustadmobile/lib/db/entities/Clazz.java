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

    //Gives the DateRange calendar Uid
    private long clazzHolidayUMCalendarUid;

    //Gives the schedule calendar ui
    private long clazzScheuleUMCalendarUid;

    //Active
    private boolean clazzActive;

    //Location
    private long clazzLocationUid;

    //Attendance
    private boolean attendanceFeature;

    //Activity
    private boolean activityFeature;

    //SEL
    private boolean selFeature;

    @UmSyncMasterChangeSeqNum
    private long clazzMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long clazzLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int clazzLastChangedBy;

    public boolean isAttendanceFeature() {
        return attendanceFeature;
    }

    public void setAttendanceFeature(boolean attendanceFeature) {
        this.attendanceFeature = attendanceFeature;
    }

    public boolean isActivityFeature() {
        return activityFeature;
    }

    public void setActivityFeature(boolean activityFeature) {
        this.activityFeature = activityFeature;
    }

    public boolean isSelFeature() {
        return selFeature;
    }

    public void setSelFeature(boolean selFeature) {
        this.selFeature = selFeature;
    }

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

    public Clazz() {
        this.attendanceFeature = true;
        this.activityFeature = true;
        this.selFeature = true;
    }

    public Clazz(String clazzName){
        this.clazzName = clazzName;
        this.attendanceFeature = true;
        this.activityFeature = true;
        this.selFeature = true;
    }

    public Clazz(String clazzName, long clazzLocationUid) {
        this.clazzName = clazzName;
        this.clazzLocationUid = clazzLocationUid;
        this.attendanceFeature = true;
        this.activityFeature = true;
        this.selFeature = true;
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
