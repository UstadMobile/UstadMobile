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

import com.ustadmobile.core.impl.UmLifecycleListener;
import com.ustadmobile.core.impl.UmLifecycleOwner;
import com.ustadmobile.core.view.UstadView;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

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

    private Hashtable arguments;

    private AtomicInteger lifecycleStatus = new AtomicInteger(0);

    public static final int NOT_CREATED = 0;

    public static final int CREATED = 1;

    public static final int STARTED = 2;

    public static final int RESUMED = 3;

    public static final int PAUSED = 4;

    public static final int STOPPED = 5;

    public static final int DESTROYED = 6;

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

        lifecycleStatus.set(CREATED);
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

        lifecycleStatus.set(STARTED);
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

        lifecycleStatus.set(RESUMED);
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

        lifecycleStatus.set(STOPPED);
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

        lifecycleStatus.set(DESTROYED);
    }


    public Hashtable getArguments() {
        return arguments;
    }

    protected void setArguments(Hashtable arguments) {
        this.arguments = arguments;
    }

    @Override
    public void addLifecycleListener(UmLifecycleListener listener) {
        lifecycleListeners.add(listener);

        switch(lifecycleStatus.get()) {
            case CREATED:
                listener.onLifecycleCreate(this);
                break;

            case STARTED:
                listener.onLifecycleStart(this);
                break;

            case RESUMED:
                listener.onLifecycleResume(this);
                break;

            case PAUSED:
                listener.onLifecyclePause(this);
                break;

            case STOPPED:
                listener.onLifecycleStop(this);
                break;

            case DESTROYED:
                listener.onLifecycleDestroy(this);
                break;
        }
    }

    @Override
    public void removeLifecycleListener(UmLifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }

}
