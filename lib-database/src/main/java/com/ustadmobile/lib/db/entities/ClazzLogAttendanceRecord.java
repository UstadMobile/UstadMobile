package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ClazzLogAttendanceRecord {

    public static final int STATUS_ATTENDED = 1;

    public static final int STATUS_ABSENT = 2;

    public static final int STATUS_PARTIAL = 4;

    @UmPrimaryKey(autoIncrement = true)
    private long clazzLogAttendanceRecordUid;

    private long clazzLogAttendanceRecordClazzLogUid;

    private long clazzLogAttendanceRecordClazzMemberUid;

    private int attendanceStatus;

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
}
