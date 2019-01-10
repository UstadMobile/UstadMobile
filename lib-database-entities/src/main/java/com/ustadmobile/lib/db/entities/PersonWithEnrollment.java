package com.ustadmobile.lib.db.entities;

public class PersonWithEnrollment extends Person {

    private long clazzUid;

    private Boolean enrolled;

    private float attendancePercentage;

    private int clazzMemberRole;

    private long personPictureUid;

    public int getClazzMemberRole() {
        return clazzMemberRole;
    }

    public void setClazzMemberRole(int clazzMemberRole) {
        this.clazzMemberRole = clazzMemberRole;
    }

    public long getClazzUid() {
        return clazzUid;
    }

    public void setClazzUid(long clazzUid) {
        this.clazzUid = clazzUid;
    }

    public Boolean getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(Boolean enrolled) {
        this.enrolled = enrolled;
    }

    public float getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(float attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public long getPersonPictureUid() {
        return personPictureUid;
    }

    public void setPersonPictureUid(long personPictureUid) {
        this.personPictureUid = personPictureUid;
    }
}
