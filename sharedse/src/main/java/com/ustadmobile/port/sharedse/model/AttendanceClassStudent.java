/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.model;

import com.ustadmobile.core.model.ListableEntity;

/**
 *
 * @author mike
 */
public class AttendanceClassStudent implements ListableEntity {
    
    public String username;
    
    public String name;

    public String full_name;

    public AttendanceClassStudent(String username, String name, String full_name) {
        this.username = username;
        this.name = name;
        this.full_name = full_name;
    }
    
    public String toString() {
        return this.full_name;
    }


    @Override
    public String getTitle() {
        return full_name;
    }

    @Override
    public String getDetail() {
        return "";
    }

    @Override
    public String getId() {
        return username;
    }

    @Override
    public String getStatusText(Object context) {
        return "";
    }

    @Override
    public int getStatusIconCode() {
        return 0;
    }
}
