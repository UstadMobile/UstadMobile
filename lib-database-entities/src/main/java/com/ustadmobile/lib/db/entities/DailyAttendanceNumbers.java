package com.ustadmobile.lib.db.entities;

public class DailyAttendanceNumbers {

    private long clazzUid;
    private long clazzLogUid;
    private long logDate;
    private float attendancePercentage;
    private float absentPercentage;
    private float partialPercentage;

    private float femaleAttendance;
    private float maleAttendance;

    public float getFemaleAttendance() {
        return femaleAttendance;
    }

    public void setFemaleAttendance(float femaleAttendance) {
        this.femaleAttendance = femaleAttendance;
    }

    public float getMaleAttendance() {
        return maleAttendance;
    }

    public void setMaleAttendance(float maleAttendance) {
        this.maleAttendance = maleAttendance;
    }

    public long getClazzUid() {
        return clazzUid;
    }

    public void setClazzUid(long clazzUid) {
        this.clazzUid = clazzUid;
    }

    public long getClazzLogUid() {
        return clazzLogUid;
    }

    public void setClazzLogUid(long clazzLogUid) {
        this.clazzLogUid = clazzLogUid;
    }

    public long getLogDate() {
        return logDate;
    }

    public void setLogDate(long logDate) {
        this.logDate = logDate;
    }

    public float getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(float attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public float getAbsentPercentage() {
        return absentPercentage;
    }

    public void setAbsentPercentage(float absentPercentage) {
        this.absentPercentage = absentPercentage;
    }

    public float getPartialPercentage() {
        return partialPercentage;
    }

    public void setPartialPercentage(float partialPercentage) {
        this.partialPercentage = partialPercentage;
    }
}
