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

    private long clazzLogClazzLogUid;

    private long studentClazzMemberUid;

    private int attendanceStatus;

    public long getClazzLogAttendanceRecordUid() {
        return clazzLogAttendanceRecordUid;
    }

    public void setClazzLogAttendanceRecordUid(long clazzLogAttendanceRecordUid) {
        this.clazzLogAttendanceRecordUid = clazzLogAttendanceRecordUid;
    }

    public long getClazzLogClazzLogUid() {
        return clazzLogClazzLogUid;
    }

    public void setClazzLogClazzLogUid(long clazzLogClazzLogUid) {
        this.clazzLogClazzLogUid = clazzLogClazzLogUid;
    }

    public long getStudentClazzMemberUid() {
        return studentClazzMemberUid;
    }

    public void setStudentClazzMemberUid(long studentClazzMemberUid) {
        this.studentClazzMemberUid = studentClazzMemberUid;
    }

    public int getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(int attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
}
