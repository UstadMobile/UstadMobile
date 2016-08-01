/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.model;

/**
 * Represents a class of students for which we take attendance
 * @author mike
 */
public class AttendanceClass {
    
    public String id;
    
    public String name;
    
    public AttendanceClass(String id, String name) {
        this.id  = id;
        this.name = name;
    }
    
    public String toString() {
        return name;
    }
    
    
}
