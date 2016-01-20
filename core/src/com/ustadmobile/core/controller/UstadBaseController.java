/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.controller;

import com.ustadmobile.core.U;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AttendanceView;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.core.view.UserSettingsView;
import java.util.Hashtable;

/**
 * Base Controller that provides key functionality 
 * 
 * @author mike
 */
public abstract class UstadBaseController implements UstadController {

    private UstadView view;
    
    protected Object context;
    
    protected boolean isDestroyed = false;
    
    public static final int CMD_ABOUT = 1001;
    
    public static final int CMD_SETTINGS = 1002;
    
    public static final int CMD_LOGOUT = 1003;
    
    public static final int CMD_ATTENDANCE = 1004;
    
    public static final int[] STANDARD_APPEMNU_CMDS = new int[]{CMD_ABOUT, 
        CMD_SETTINGS, CMD_LOGOUT, CMD_ATTENDANCE};
    
    public static final int[] STANDARD_APPMENU_STRIDS = new int[]{U.id.about,
        U.id.settings, U.id.logout, U.id.attendance};
    
    /**
     * Create a new controller with the given context
     * 
     * @param context System dependent context object
     */
    public UstadBaseController(Object context) {
        this.context = context;
    }
    
    /**
     * Set the view that this controller is associated with
     * 
     * @param view 
     */
    public void setView(UstadView view) {
        this.view = view;
    }

    /**
     * Get the view this controller is associated with
     * @return View this controller is associated with
     */
    public UstadView getView() {
        return this.view;
    }
    
    /**
     * Get the system dependent context for this controller
     * 
     * @return System dependent context object for this controller
     */
    public Object getContext() {
        return this.context;
    }
    
    /**
     * Must call all view methods that set UI strings - e.g.  when the
     * locale is changed
     */
    public abstract void setUIStrings();
    
    /**
     * This should be called by the view when it is paused: e.g. when the user
     * leaves the view
     */
    public void handleViewPause() {
        
    }
    
    /**
     * This should be called by the view when the user has come back to
     * the view
     */
    public void handleViewResume() {
        
    }
    
    /**
     * This should be called by the view when it is being destroyed: this is
     * irreversible and it is time to stop background activities 
     */
    public void handleViewDestroy() {
        setDestroyed(true);
    }
    
    /**
     * Returns true if the view we are working for has been destroyed, false
     * otherwise
     * 
     * @return true if view has been destroyed, false otherwise
     */
    protected synchronized boolean isDestroyed() {
        return isDestroyed;
    }
    
    /**
     * Set if the view has been destroyed - this is in reality irreversible
     * and lives in a synchronized method for purposes of thread safety
     * 
     * @param isDestroyed 
     */
    protected synchronized void setDestroyed(boolean isDestroyed) {
        this.isDestroyed = isDestroyed;
    }
    
    public boolean handleClickAppMenuItem(int cmdId) {
        switch(cmdId) {
            case CMD_ABOUT:
                //do nothing yet
                return true;
            case CMD_SETTINGS:
                UstadMobileSystemImpl.getInstance().go(UserSettingsView.class, 
                    new Hashtable(), context);
                return true;
            case CMD_LOGOUT:
                LoginController.handleLogout(context);
                return true;
            case CMD_ATTENDANCE:
                UstadMobileSystemImpl.getInstance().go(AttendanceView.class,
                    new Hashtable(), context);
                return true;
        }
        
        return false;
    }
    
    public void setStandardAppMenuOptions() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] labels = new String[STANDARD_APPEMNU_CMDS.length];
        for(int i = 0; i < labels.length; i++) {
            labels[i] = impl.getString(STANDARD_APPMENU_STRIDS[i]);
        }
        
        view.setAppMenuCommands(labels, STANDARD_APPEMNU_CMDS);
    }
    
    
}
