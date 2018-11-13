package com.ustadmobile.lib.db.entities;

public class ClazzAverage {

    private int numClazzes;
    private int numStudents;
    private int numTeachers;

    public float getAttendanceAverage() {
        return attendanceAverage;
    }

    public void setAttendanceAverage(float attendanceAverage) {
        this.attendanceAverage = attendanceAverage;
    }

    private float attendanceAverage;

    public int getNumClazzes() {
        return numClazzes;
    }

    public void setNumClazzes(int numClazzes) {
        this.numClazzes = numClazzes;
    }

    public int getNumStudents() {
        return numStudents;
    }

    public void setNumStudents(int numStudents) {
        this.numStudents = numStudents;
    }

    public int getNumTeachers() {
        return numTeachers;
    }

    public void setNumTeachers(int numTeachers) {
        this.numTeachers = numTeachers;
    }


}
