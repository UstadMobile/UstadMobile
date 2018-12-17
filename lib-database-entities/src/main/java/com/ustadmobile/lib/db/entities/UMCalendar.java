package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents a Caledar which will be liked to multiple holidays, schedules etc
 * Its basically a collection of dates and time. (holidays and schedules)
 */
@UmEntity(tableId = 28)
public class UMCalendar implements SyncableEntity{


    @UmPrimaryKey(autoGenerateSyncable = true)
    private long umCalendarUid;

    //The name of this calendar
    private String umCalendarName;


    //todo: seems like wrong prefix
    @UmSyncMasterChangeSeqNum
    private long personMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long personLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int umCalendarLastChangedBy;

    public long getPersonMasterChangeSeqNum() {
        return personMasterChangeSeqNum;
    }

    public void setPersonMasterChangeSeqNum(long personMasterChangeSeqNum) {
        this.personMasterChangeSeqNum = personMasterChangeSeqNum;
    }

    public long getPersonLocalChangeSeqNum() {
        return personLocalChangeSeqNum;
    }

    public void setPersonLocalChangeSeqNum(long personLocalChangeSeqNum) {
        this.personLocalChangeSeqNum = personLocalChangeSeqNum;
    }

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

    public int getUmCalendarLastChangedBy() {
        return umCalendarLastChangedBy;
    }

    public void setUmCalendarLastChangedBy(int umCalendarLastChangedBy) {
        this.umCalendarLastChangedBy = umCalendarLastChangedBy;
    }
}
