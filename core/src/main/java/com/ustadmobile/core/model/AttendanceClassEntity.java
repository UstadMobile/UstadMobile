package com.ustadmobile.core.model;

/**
 * Created by mike on 14/11/16.
 */

public class AttendanceClassEntity implements ListableEntity {

    private String className;

    public AttendanceClassEntity(String className) {
        this.className = className;
    }

    @Override
    public String getTitle() {
        return className;
    }

    @Override
    public String getDetail() {
        return "--";
    }
}
