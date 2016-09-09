/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.UstadView;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
    import org.json.*;
/* $endif$ */

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

    /**
     * Indicates the tab for items already downloaded
     */
    public static final int INDEX_DOWNLOADEDENTRIES = 0;

    /**
     * Indicates the tab for browsing OPDS feeds
     */
    public static final int INDEX_BROWSEFEEDS = 1;

    /**
     * Indicates the tab for class management
     */
    public static final int INDEX_CLASSES = 1;

    public static final int NUM_CATALOG_TABS = 1;

    private Hashtable args;
    
    public BasePointController(Object context) {
        super(context);
    }
    
    public static BasePointController makeControllerForView(BasePointView view, Hashtable args) {
        BasePointController ctrl = new BasePointController(view.getContext());
        ctrl.args = args;
        ctrl.setView(view);
        view.setClassListVisible(ctrl.isUserTeacher());
        return ctrl;
    }
    
    public static Hashtable makeDefaultBasePointArgs(Object context) {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] basePointURLs = new String[] {impl.getBasePointDefaultCatalogURL()};
        
        String iPrefix;
        for(int i = 0; i < BasePointController.NUM_CATALOG_TABS; i++) {
            iPrefix = i+BasePointController.OPDS_ARGS_PREFIX;
            args.put(iPrefix + CatalogController.KEY_URL, basePointURLs[i]);
            args.put(iPrefix + CatalogController.KEY_HTTPUSER, 
                impl.getActiveUser(context));
            args.put(iPrefix + CatalogController.KEY_HTTPPPASS, 
                impl.getActiveUserAuth(context));
            args.put(iPrefix + CatalogController.KEY_FLAGS, 
                new Integer(CatalogController.CACHE_ENABLED));
            args.put(iPrefix + CatalogController.KEY_RESMOD, 
                new Integer(CatalogController.USER_RESOURCE | CatalogController.SHARED_RESOURCE));

            //by default show the browse button on the first tab only
            if(i == 0 && impl.getBasePointBrowseURL() != null) {
                args.put(iPrefix + CatalogController.KEY_BROWSE_BUTTON_URL,
                        impl.getBasePointBrowseURL());
            }
        }
        
        Integer downloadedEntriesFlags = new Integer(
            CatalogController.CACHE_ENABLED | CatalogController.SORT_DESC | 
            CatalogController.SORT_BY_LASTACCESSED);
        args.put(INDEX_DOWNLOADEDENTRIES+BasePointController.OPDS_ARGS_PREFIX +
            CatalogController.KEY_FLAGS, downloadedEntriesFlags);
        
        return args;
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


    /**
     * Determines if the current user is a teacher (e.g. would see
     * class list management)
     *
     * TODO: Implement this
     *
     * @return true if user is teacher, false otherwise
     */
    public boolean isUserTeacher() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String classListJSON = impl.getUserPref("teacherclasslist", context);

        if(classListJSON == null) {
            return false;//not a teacher or no classes assigned
        }else {
            return true;
        }
    }

}
