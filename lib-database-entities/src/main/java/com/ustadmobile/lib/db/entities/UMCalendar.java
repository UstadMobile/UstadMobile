package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a Caledar which will be liked to multiple holidays, schedules etc
 * Its basically a collection of dates and time. (holidays and schedules)
 */
@UmEntity
public class UMCalendar implements SyncableEntity{

    @UmPrimaryKey(autoIncrement = true)
    private long umCalendarUid;

    //The name of this calendar
    private String umCalendarName;


    public long getUmCalendarUid() {
        return umCalendarUid;
    }

    public void setUmCalendarUid(long umCalendarUid) {
        this.umCalendarUid = umCalendarUid;
    }

    public String getUmCalendarName() {
        return umCalendarName;
    }

    public void setUmCalendarName(String umCalendarName) {
        this.umCalendarName = umCalendarName;
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
