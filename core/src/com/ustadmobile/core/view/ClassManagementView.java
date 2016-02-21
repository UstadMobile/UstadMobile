/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.view;

import com.ustadmobile.core.model.AttendanceClassStudent;

/**
 *
 * @author varuna
 */
public interface ClassManagementView extends UstadView{
    
    public void setClassName(String className);
    
    public void setStudentList(AttendanceClassStudent[] students);
    
    public void setAttendanceLabel(String attendanceLabel);
    
    public void setExamsLabel(String examsLabel);
    
    public void setReportsLabel(String reportsLabel);
    
}
