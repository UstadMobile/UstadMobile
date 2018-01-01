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

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.UserSettingsView2;
import com.ustadmobile.core.view.UstadView;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Base Controller that provides key functionality for any view :
 * it contains the data or a reference to the data and handles events and
 * logic.
 * 
 * @author mike
 */
public abstract class UstadBaseController implements UstadController {

    private UstadView view;
    
    protected Object context;
    
    protected boolean isDestroyed = false;

    protected Vector controllerLifecycleListeners;
    
    public static final int CMD_ABOUT = 1001;
    
    public static final int CMD_SETTINGS = 1002;
    
    public static final int CMD_LOGOUT = 1003;
    
    public static final int CMD_HOME = 1004;

    public static final int[] STANDARD_APPEMNU_CMDS = new int[]{CMD_HOME, 
        CMD_ABOUT, CMD_SETTINGS, CMD_LOGOUT};
    
    public static final int[] STANDARD_APPMENU_STRIDS = new int[]{MessageID.home,
        MessageID.about, MessageID.settings,MessageID.logout};


    /**
     * Create a new controller with the given context
     * 
     * @param context System dependent context object
     * @param statusEventListeningEnabled Whether or not to register for status event updates and pass to the view
     */
    public UstadBaseController(Object context, boolean statusEventListeningEnabled) {
        this.context = context;
    }

    /**
     * Create a new controller with the given context
     *
     * @param context System dependent context objec
     */
    public UstadBaseController(Object context) {
        this(context, true);
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
     * Called when the view is destroyed and removed from memory. onDestroy in Android, when the form
     * is navigated away from in J2ME
     */
    public void onDestroy() {
        if(controllerLifecycleListeners == null)
            return;

        for(int i = 0; i < controllerLifecycleListeners.size(); i++) {
            ((ControllerLifecycleListener)controllerLifecycleListeners.elementAt(i)).onDestroyed(this);
        }
    }

    public void onStop() {

    }

    public void onStart() {

    }


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
    
    /**
     * Handle when users have clicked a standard option from the menu
     * 
     * Note: This method is static so it can be used without a controller object
     * (e.g. in the event a controller failed to load)
     * 
     * @param cmdId - Command ID - CMD_ABOUT, CMD_SETTINGS or CMD_LOGOUT
     * @param context - Platform context object
     * @return true if the command id matches something we know about and it was handled, false otherwise
     */
    public static boolean handleClickAppMenuItem(int cmdId, Object context) {
        switch(cmdId) {
            case CMD_ABOUT:
                UstadMobileSystemImpl.getInstance().go(AboutView.VIEW_NAME, new Hashtable(), context);
                return true;
            case CMD_SETTINGS:
                UstadMobileSystemImpl.getInstance().go(UserSettingsView2.VIEW_NAME,null, context);
                return true;
            case CMD_LOGOUT:
                LoginController.handleLogout(context);
                return true;
        }
        
        return false;
    }
    
    public boolean handleClickAppMenuItem(int cmdId) {
        return handleClickAppMenuItem(cmdId, getContext());
    }
    
    /**
     * Fills in the standard menu options into the given arrays
     * 
     * @param cmds Array to be filled with the commands as per STANDARD_APPEMNU_CMDS
     * @param labels Array of be filled with the labels for each command
     * @param offset Offset in array to start filling from
     */
    protected void fillStandardMenuOptions(int[] cmds, String[] labels, int offset) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        for(int i = offset; i < STANDARD_APPEMNU_CMDS.length + offset; i++) {
            cmds[i] = STANDARD_APPEMNU_CMDS[i - offset];
            labels[i] = impl.getString(STANDARD_APPMENU_STRIDS[i - offset], getContext());
        }
    }
    
    public void setStandardAppMenuOptions() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] labels = new String[STANDARD_APPEMNU_CMDS.length];
        fillStandardMenuOptions(new int[labels.length], labels, 0);
        view.setAppMenuCommands(labels, STANDARD_APPEMNU_CMDS);
    }

    public void addLifecycleListener(ControllerLifecycleListener listener) {
        if(controllerLifecycleListeners == null)
            controllerLifecycleListeners = new Vector();

        controllerLifecycleListeners.addElement(listener);
    }

    public void removeLifecycleListener(ControllerLifecycleListener listener) {
        controllerLifecycleListeners.removeElement(listener);
    }







}
