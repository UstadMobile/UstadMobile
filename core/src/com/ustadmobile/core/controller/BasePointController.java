/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.UstadView;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * The base point is right now basically a wrapper containing two catalogs
 * 
 * 
 * @author mike
 */
public class BasePointController extends UstadBaseController{

    private BasePointView basePointView;
    
    /**
     * The arguments given to this class are passed down to create two
     * catalog views: e.g. 0-opds-url -> http://server.com/file.opds
     * 
     * Will get passed down as url -> http://server.com/file.opds
     */
    public static final String OPDS_ARGS_PREFIX = "-opds";
    
    public static final int NUM_TABS = 2;
    
    /**
     * 
     */
    public static final int INDEX_DOWNLOADEDENTRIES = 0;
    
    public static final int INDEX_BROWSEFEEDS = 1;
    
    private Hashtable args;
    
    public BasePointController(Object context) {
        super(context);
    }
    
    public static BasePointController makeControllerForView(BasePointView view, Hashtable args) {
        BasePointController ctrl = new BasePointController(view.getContext());
        ctrl.args = args;
        ctrl.setView(view);
        return ctrl;
    }
    
    /**
     * For use by the related view: generate the required arguments
     * 
     * @param position
     * 
     * @return 
     */
    public Hashtable getCatalogOPDSArguments(int position) {
        Enumeration keys = this.args.keys();
        Hashtable result = new Hashtable();
        String keyVal;
        String prefix = position + OPDS_ARGS_PREFIX;
        int prefixLen = prefix.length();
        while(keys.hasMoreElements()) {
            keyVal = (String)keys.nextElement();
            if(keyVal.startsWith(prefix)) {
                result.put(keyVal.substring(prefixLen), args.get(keyVal));
            }
        }
        
        return result;
    }
    
    public void setView(UstadView view) {
        if(view instanceof BasePointView) {
            super.setView(view);
            this.basePointView = (BasePointView)view;
        }else {
            throw new IllegalArgumentException("Must be basepointview");
        }
    }
    
    public void setUIStrings() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void handleAddFeed(String url, int mode) {
        
    }
    
    
}
