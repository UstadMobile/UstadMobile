/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.model;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.ListableEntity;
import com.ustadmobile.port.sharedse.controller.AttendanceController;

/**
 * Represents a class of students for which we take attendance
 * @author mike
 */
public class AttendanceClass implements ListableEntity {
    
    public String id;
    
    public String name;

    public int syncStatus;

    public static final int[] SYNCSTATUS_TO_ICONS = new int[] {
        ListableEntity.STATUSICON_ATTENTION, //NOT_TAKEN
        ListableEntity.STATUSICON_SENDING,
        ListableEntity.STATUSICON_SENT
    };

    public AttendanceClass(String id, String name) {
        this.id  = id;
        this.name = name;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getDetail() {
        return "";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getStatusText(Object context) {
        int messageCode = 0;
        switch(syncStatus){
            case AttendanceController.STATUS_ATTENDANCE_NOT_TAKEN:
                messageCode = MessageID.not_taken;
                break;
            case AttendanceController.STATUS_ATTENDANCE_TAKEN:
                messageCode = MessageID.sending;
                break;
            case AttendanceController.STATUS_ATTENDANCE_SENT:
                messageCode = MessageID.sent;
                break;
        }
        return UstadMobileSystemImpl.getInstance().getString(messageCode, context);
    }

    @Override
    public int getStatusIconCode() {
        return SYNCSTATUS_TO_ICONS[syncStatus];
    }

    public String toString() {
        return name;
    }
    
    
}
