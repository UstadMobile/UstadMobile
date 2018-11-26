package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 17)
public class Holiday {


    @UmPrimaryKey(autoGenerateSyncable = true)
    private long holidayUid;

    //Represents the Calendar set this holiday belongs to.
    private long holidayUMCalendarUid;

    //The actual day (te?) of the holiday
    private long holidayDate;

    //The name of this holiday set.
    private long holidayName;

    @UmSyncLocalChangeSeqNum
    private long holidayLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long holidayMasterChangeSeqNum;

    public long getHolidayMasterChangeSeqNum() {
        return holidayMasterChangeSeqNum;
    }

    public void setHolidayMasterChangeSeqNum(long holidayMasterChangeSeqNum) {
        this.holidayMasterChangeSeqNum = holidayMasterChangeSeqNum;
    }

    public long getHolidayLocalChangeSeqNum() {

        return holidayLocalChangeSeqNum;
    }

    public void setHolidayLocalChangeSeqNum(long holidayLocalChangeSeqNum) {
        this.holidayLocalChangeSeqNum = holidayLocalChangeSeqNum;
    }

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

}
