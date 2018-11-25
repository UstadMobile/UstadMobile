package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 15)
public class ClazzLogAttendanceRecord {

    //Attendance statuses for every Person's Entry in every Class Attendance Log Entry:

    public static final int STATUS_ATTENDED = 1;
    public static final int STATUS_ABSENT = 2;
    public static final int STATUS_PARTIAL = 4;

    @UmPrimaryKey(autoIncrement = true)
    private long clazzLogAttendanceRecordUid;

    private long clazzLogAttendanceRecordClazzLogUid;

    private long clazzLogAttendanceRecordClazzMemberUid;

    private int attendanceStatus;

    @UmSyncMasterChangeSeqNum
    private long clazzLogAttendanceRecordMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long clazzLogAttendanceRecordLocalChangeSeqNum;

    public long getClazzLogAttendanceRecordUid() {
        return clazzLogAttendanceRecordUid;
    }

    public void setClazzLogAttendanceRecordUid(long clazzLogAttendanceRecordUid) {
        this.clazzLogAttendanceRecordUid = clazzLogAttendanceRecordUid;
    }

    public long getClazzLogAttendanceRecordClazzLogUid() {
        return clazzLogAttendanceRecordClazzLogUid;
    }

    public void setClazzLogAttendanceRecordClazzLogUid(long clazzLogAttendanceRecordClazzLogUid) {
        this.clazzLogAttendanceRecordClazzLogUid = clazzLogAttendanceRecordClazzLogUid;
    }

    public long getClazzLogAttendanceRecordClazzMemberUid() {
        return clazzLogAttendanceRecordClazzMemberUid;
    }

    public void setClazzLogAttendanceRecordClazzMemberUid(long clazzLogAttendanceRecordClazzMemberUid) {
        this.clazzLogAttendanceRecordClazzMemberUid = clazzLogAttendanceRecordClazzMemberUid;
    }

    public int getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(int attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public long getClazzLogAttendanceRecordMasterChangeSeqNum() {
        return clazzLogAttendanceRecordMasterChangeSeqNum;
    }

    public void setClazzLogAttendanceRecordMasterChangeSeqNum(long clazzLogAttendanceRecordMasterChangeSeqNum) {
        this.clazzLogAttendanceRecordMasterChangeSeqNum = clazzLogAttendanceRecordMasterChangeSeqNum;
    }

    public long getClazzLogAttendanceRecordLocalChangeSeqNum() {
        return clazzLogAttendanceRecordLocalChangeSeqNum;
    }

    public void setClazzLogAttendanceRecordLocalChangeSeqNum(long clazzLogAttendanceRecordLocalChangeSeqNum) {
        this.clazzLogAttendanceRecordLocalChangeSeqNum = clazzLogAttendanceRecordLocalChangeSeqNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClazzLogAttendanceRecord that = (ClazzLogAttendanceRecord) o;

        if (clazzLogAttendanceRecordUid != that.clazzLogAttendanceRecordUid) return false;
        if (clazzLogAttendanceRecordClazzLogUid != that.clazzLogAttendanceRecordClazzLogUid)
            return false;
        if (clazzLogAttendanceRecordClazzMemberUid != that.clazzLogAttendanceRecordClazzMemberUid)
            return false;
        return attendanceStatus == that.attendanceStatus;
    }

    @Override
    public int hashCode() {
        int result = (int) (clazzLogAttendanceRecordUid ^ (clazzLogAttendanceRecordUid >>> 32));
        result = 31 * result + (int) (clazzLogAttendanceRecordClazzLogUid ^ (clazzLogAttendanceRecordClazzLogUid >>> 32));
        result = 31 * result + (int) (clazzLogAttendanceRecordClazzMemberUid ^ (clazzLogAttendanceRecordClazzMemberUid >>> 32));
        result = 31 * result + attendanceStatus;
        return result;
    }
}
