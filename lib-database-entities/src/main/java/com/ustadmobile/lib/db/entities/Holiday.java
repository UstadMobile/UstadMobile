package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class Holiday implements SyncableEntity{


    @UmPrimaryKey(autoIncrement = true)
    private long holidayUid;

    //Represents the Calendar set this holiday belongs to.
    private long holidayUMCalendarUid;

    //The actual day (te?) of the holiday
    private long holidayDate;

    //The name of this holiday set.
    private long holidayName;


    public long getHolidayUMCalendarUid() {
        return holidayUMCalendarUid;
    }

    public void setHolidayUMCalendarUid(long holidayUMCalendarUid) {
        this.holidayUMCalendarUid = holidayUMCalendarUid;
    }

    public long getHolidayUid() {
        return holidayUid;
    }

    public void setHolidayUid(long holidayUid) {
        this.holidayUid = holidayUid;
    }

    public long getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(long holidayDate) {
        this.holidayDate = holidayDate;
    }

    public long getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(long holidayName) {
        this.holidayName = holidayName;
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
