package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class Schedule implements SyncableEntity {

    public static final int SCHEDULE_FREQUENCY_ONCE = 1;
    public static final int SCHEDULE_FREQUENCY_WEEKLY = 2;
    public static final int SCHEDULE_FREQUENCY_MONTHLY = 3;
    public static final int SCHEDULE_FREQUENCY_YEARLY = 4;

    public static final int MONTH_JANUARY = 1;
    public static final int MONTH_FEBUARY = 2;
    public static final int MONTH_MARCH = 3;
    public static final int MONTH_APRIL = 4;
    public static final int MONTH_MAY = 5;
    public static final int MONTH_JUNE = 6;
    public static final int MONTH_JULY = 7;
    public static final int MONTH_AUGUST = 8;
    public static final int MONTH_SEPTEMBER = 9;
    public static final int MONTH_OCTOBER = 10;
    public static final int MONTH_NOVEMBER = 11;
    public static final int MONTH_DECEMBER = 12;


    @UmPrimaryKey(autoIncrement = true)
    private long scheduleUid;

    private long sceduleStartTime;

    private long scheduleEndTime;

    private int scheduleDay;

    private int scheuleMoth;

    private int scheduleYear;

    // Frequency - Once, Every Week, Every Month, Every Year
    private int scheduleFrequency;

    //The Calendar this will be set to.
    private long umCalendarUid;


    public long getSceduleStartTime() {
        return sceduleStartTime;
    }

    public void setSceduleStartTime(long sceduleStartTime) {
        this.sceduleStartTime = sceduleStartTime;
    }

    public long getScheduleEndTime() {
        return scheduleEndTime;
    }

    public void setScheduleEndTime(long scheduleEndTime) {
        this.scheduleEndTime = scheduleEndTime;
    }

    public int getScheduleDay() {
        return scheduleDay;
    }

    public void setScheduleDay(int scheduleDay) {
        this.scheduleDay = scheduleDay;
    }

    public int getScheuleMoth() {
        return scheuleMoth;
    }

    public void setScheuleMoth(int scheuleMoth) {
        this.scheuleMoth = scheuleMoth;
    }

    public int getScheduleYear() {
        return scheduleYear;
    }

    public void setScheduleYear(int scheduleYear) {
        this.scheduleYear = scheduleYear;
    }

    public int getScheduleFrequency() {
        return scheduleFrequency;
    }

    public void setScheduleFrequency(int scheduleFrequency) {
        this.scheduleFrequency = scheduleFrequency;
    }

    public long getUmCalendarUid() {
        return umCalendarUid;
    }

    public void setUmCalendarUid(long umCalendarUid) {
        this.umCalendarUid = umCalendarUid;
    }

    public long getScheduleUid() {
        return scheduleUid;
    }

    public void setScheduleUid(long scheduleUid) {
        this.scheduleUid = scheduleUid;
    }

    @Override
    public long getMasterChangeSeqNum() {
        return 0;
    }

    @Override
    public void setMasterChangeSeqNum(long masterChangeSeqNum) {

    }

    @Override
    public long getLocalChangeSeqNum() {
        return 0;
    }

    @Override
    public void setLocalChangeSeqNum(long localChangeSeqNum) {

    }
}
