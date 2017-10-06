/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.UstadView;

import java.util.Hashtable;

/**
 *
 * @author mike
 */
public class LoadControllerThread extends Thread{
    
    private Hashtable args;
    
    private AsyncLoadableController controllerToLoad;
    
    private ControllerReadyListener listener;
    
    private UstadView view;
    
    public LoadControllerThread(Hashtable args, AsyncLoadableController controller, ControllerReadyListener listener, UstadView view) {
        this.args = args;
        this.controllerToLoad = controller;
        this.listener = listener;
        this.view = view;
    }
    
    public void run() {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 595, null);
        
        UstadController result = null;
        try {
            result = controllerToLoad.loadController(args, view.getContext());
            if(this.view != null) {
                result.setView(view);
            }
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 161, null, e);
        }
        
        listener.controllerReady(result, 0);
    }
    
}
