package com.ustadmobile.lib.db.entities;

public class ClazzWithNumStudents extends Clazz {

    private int numStudents;

    private int numTeachers;

    private String teacherNames;

    public int getNumTeachers() {
        return numTeachers;
    }

    public void setNumTeachers(int numTeachers) {
        this.numTeachers = numTeachers;
    }

    public String getTeacherNames() {
        return teacherNames;
    }

    public void setTeacherNames(String teacherNames) {
        this.teacherNames = teacherNames;
    }

    public int getNumStudents() {
        return numStudents;
    }

    public void setNumStudents(int numStudents) {
        this.numStudents = numStudents;
    }
}
