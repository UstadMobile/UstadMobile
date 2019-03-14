package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 17)
public class DateRange {


    @UmPrimaryKey(autoGenerateSyncable = true)
    private long dateRangeUid;

    @UmSyncLocalChangeSeqNum
    private long dateRangeLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long dateRangeMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int dateRangLastChangedBy;

    private long dateRangeFromDate;

    private long dateRangeToDate;

    private long dateRangeUMCalendarUid;

    private boolean dateRangeActive;

    String dateRangeName;

    String dateRangeDesc;

    public DateRange(long fromDate, long toDate){
        this.dateRangeFromDate = fromDate;
        this.dateRangeToDate = toDate;
        this.dateRangeActive = true;
    }

    public DateRange(long fromDate){
        this.dateRangeFromDate = fromDate;
        this.dateRangeActive = true;
    }

    public DateRange(){
        this.dateRangeActive = true;
    }

    public long getDateRangeUid() {
        return dateRangeUid;
    }

    public void setDateRangeUid(long dateRangeUid) {
        this.dateRangeUid = dateRangeUid;
    }

    public long getDateRangeLocalChangeSeqNum() {
        return dateRangeLocalChangeSeqNum;
    }

    public void setDateRangeLocalChangeSeqNum(long dateRangeLocalChangeSeqNum) {
        this.dateRangeLocalChangeSeqNum = dateRangeLocalChangeSeqNum;
    }

    public long getDateRangeMasterChangeSeqNum() {
        return dateRangeMasterChangeSeqNum;
    }

    public void setDateRangeMasterChangeSeqNum(long dateRangeMasterChangeSeqNum) {
        this.dateRangeMasterChangeSeqNum = dateRangeMasterChangeSeqNum;
    }

    public int getDateRangLastChangedBy() {
        return dateRangLastChangedBy;
    }

    public void setDateRangLastChangedBy(int dateRangLastChangedBy) {
        this.dateRangLastChangedBy = dateRangLastChangedBy;
    }

    public long getDateRangeFromDate() {
        return dateRangeFromDate;
    }

    public void setDateRangeFromDate(long dateRangeFromDate) {
        this.dateRangeFromDate = dateRangeFromDate;
    }

    public long getDateRangeToDate() {
        return dateRangeToDate;
    }

    public void setDateRangeToDate(long dateRangeToDate) {
        this.dateRangeToDate = dateRangeToDate;
    }

    public long getDateRangeUMCalendarUid() {
        return dateRangeUMCalendarUid;
    }

    public void setDateRangeUMCalendarUid(long dateRangeUMCalendarUid) {
        this.dateRangeUMCalendarUid = dateRangeUMCalendarUid;
    }

    public String getDateRangeName() {
        return dateRangeName;
    }

    public void setDateRangeName(String dateRangeName) {
        this.dateRangeName = dateRangeName;
    }

    public String getDateRangeDesc() {
        return dateRangeDesc;
    }

    public void setDateRangeDesc(String dateRangeDesc) {
        this.dateRangeDesc = dateRangeDesc;
    }

    public boolean isDateRangeActive() {
        return dateRangeActive;
    }

    public void setDateRangeActive(boolean dateRangeActive) {
        this.dateRangeActive = dateRangeActive;
    }
}
