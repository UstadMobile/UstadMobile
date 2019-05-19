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
public class UMCalendar{

    public static final int CATEGORY_HOLIDAY = 1;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long umCalendarUid;

    //The name of this calendar
    private String umCalendarName;

    //Category
    private int umCalendarCategory;

    public boolean isUmCalendarActive() {
        return umCalendarActive;
    }

    public void setUmCalendarActive(boolean umCalendarActive) {
        this.umCalendarActive = umCalendarActive;
    }

    //active
    private boolean umCalendarActive;

    //Tester method- Please remove me later
    private boolean umCalendarFlag;

    public UMCalendar(String name, int category){
        this.umCalendarName = name;
        this.umCalendarCategory = category;
        this.umCalendarActive = true;
    }

    public UMCalendar(){

    }

    @UmSyncMasterChangeSeqNum
    private long umCalendarMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long umCalendarLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int umCalendarLastChangedBy;


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

    public int getUmCalendarCategory() {
        return umCalendarCategory;
    }

    public void setUmCalendarCategory(int umCalendarCategory) {
        this.umCalendarCategory = umCalendarCategory;
    }

    public long getUmCalendarMasterChangeSeqNum() {
        return umCalendarMasterChangeSeqNum;
    }

    public void setUmCalendarMasterChangeSeqNum(long umCalendarMasterChangeSeqNum) {
        this.umCalendarMasterChangeSeqNum = umCalendarMasterChangeSeqNum;
    }

    public long getUmCalendarLocalChangeSeqNum() {
        return umCalendarLocalChangeSeqNum;
    }

    public void setUmCalendarLocalChangeSeqNum(long umCalendarLocalChangeSeqNum) {
        this.umCalendarLocalChangeSeqNum = umCalendarLocalChangeSeqNum;
    }

    public int getUmCalendarLastChangedBy() {
        return umCalendarLastChangedBy;
    }

    public void setUmCalendarLastChangedBy(int umCalendarLastChangedBy) {
        this.umCalendarLastChangedBy = umCalendarLastChangedBy;
    }

    public boolean isUmCalendarFlag() {
        return umCalendarFlag;
    }

    public void setUmCalendarFlag(boolean umCalendarFlag) {
        this.umCalendarFlag = umCalendarFlag;
    }
}
