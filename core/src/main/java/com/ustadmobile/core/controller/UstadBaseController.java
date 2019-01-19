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
import com.ustadmobile.core.impl.UmLifecycleListener;
import com.ustadmobile.core.impl.UmLifecycleOwner;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AboutView;
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
public abstract class UstadBaseController<V extends UstadView> implements UstadController<V>,
        UmLifecycleOwner {

    protected V view;
    
    protected Object context;

    protected final Vector<UmLifecycleListener> lifecycleListeners = new Vector<>();

    public static final int CMD_ABOUT = 1001;
    
    public static final int CMD_SETTINGS = 1002;
    
    public static final int CMD_LOGOUT = 1003;
    
    public static final int CMD_HOME = 1004;

    public static final int[] STANDARD_APPEMNU_CMDS = new int[]{CMD_HOME, 
        CMD_ABOUT, CMD_SETTINGS, CMD_LOGOUT};
    
    public static final int[] STANDARD_APPMENU_STRIDS = new int[]{MessageID.home,
        MessageID.about, MessageID.settings,MessageID.logout};

    private Hashtable arguments;

    /**
     * Create a new controller with the given context
     *
     * @param context System dependent context objec
     */
    public UstadBaseController(Object context) {
        this.context = context;
    }

    public UstadBaseController(Object context, Hashtable arguments, V view) {
        this.context = context;
        this.arguments = arguments;
        this.view = view;
    }

    
    /**
     * Set the view that this controller is associated with
     * 
     * @param view 
     */
    public void setView(V view) {
        this.view = view;
    }

    /**
     * Get the view this controller is associated with
     * @return View this controller is associated with
     */
    public V getView() {
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
     * Handle when the presenter is created. Analogous to Android's onCreate
     *
     * @param savedState savedState if any
     */
    public void onCreate(Hashtable savedState) {
        synchronized (lifecycleListeners) {
            for(UmLifecycleListener listener : lifecycleListeners) {
                listener.onLifecycleCreate(this);
            }
        }
    }

    /**
     * Handle when the presenter is about to become visible. Analogous to Android's onStart
     */
    public void onStart() {
        synchronized (lifecycleListeners) {
            for(UmLifecycleListener listener : lifecycleListeners) {
                listener.onLifecycleStart(this);
            }
        }
    }

    /**
     * Handle when the presenter has become visible. Analogous to Android's onResume
     */
    public void onResume() {
        synchronized (lifecycleListeners) {
            for(UmLifecycleListener listener : lifecycleListeners) {
                listener.onLifecycleResume(this);
            }
        }
    }

    /**
     * Handle when the presenter is no longer visible. Analogous to Android's onStop
     */
    public void onStop() {
        synchronized (lifecycleListeners) {
            for(UmLifecycleListener listener : lifecycleListeners) {
                listener.onLifecycleStop(this);
            }
        }
    }

    /**
     * Called when the view is destroyed and removed from memory. Analogous to Android's onDestroy
     */
    public void onDestroy() {
        synchronized (lifecycleListeners) {
            for(UmLifecycleListener listener : lifecycleListeners) {
                listener.onLifecycleDestroy(this);
            }
        }
    }



    /**
     * Must call all view methods that set UI strings - e.g.  when the
     * locale is changed
     */
    public abstract void setUIStrings();
    
    /**
     * This should be called by the view when it is being destroyed: this is
     * irreversible and it is time to stop background activities 
     */
    public void handleViewDestroy() {

    }
    

    public Hashtable getArguments() {
        return arguments;
    }

    protected void setArguments(Hashtable arguments) {
        this.arguments = arguments;
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

    @Override
    public void addLifecycleListener(UmLifecycleListener listener) {
        lifecycleListeners.add(listener);
    }

    @Override
    public void removeLifecycleListener(UmLifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }
}
