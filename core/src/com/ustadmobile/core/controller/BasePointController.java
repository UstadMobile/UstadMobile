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
    
    public static final int NUM_TABS = 2;
    
    /**
     * 
     */
    public static final int INDEX_DOWNLOADEDENTRIES = 0;
    
    public static final int INDEX_BROWSEFEEDS = 1;
    
    public static final int OPDS_FEEDS_INDEX_URL = 0;
    
    public static final int OPDS_FEEDS_INDEX_TITLE = 1;
    
    public static final int OPDS_SELECTPROMPT = 0;
    
    public static final int OPDS_CUSTOM = 1;
    
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
    
    public static Hashtable makeDefaultBasePointArgs(Object context) {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] basePointURLs = new String[] {
            CatalogController.OPDS_PROTO_DEVICE, 
            CatalogController.OPDS_PROTO_USER_FEEDLIST
        };
        
        String iPrefix;
        for(int i = 0; i < BasePointController.NUM_TABS; i++) {
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
    
    public void handleClickAddFeed() {
        basePointView.showAddFeedDialog();
    }
    
    public void handleFeedPresetSelected(int index) {
        if(index > OPDS_CUSTOM) {
            String[] selectedPreset = UstadMobileConstants.OPDS_FEEDS_PRESETS[index];
            basePointView.setAddFeedDialogTitle(selectedPreset[OPDS_FEEDS_INDEX_TITLE]);
            basePointView.setAddFeedDialogURL(selectedPreset[OPDS_FEEDS_INDEX_URL]);
        }
    }
    
    /**
     * Return a one dimensional string array for the prepopulated OPDS_FEEDS_PRESETS
     * of common OPDS sources
     * 
     * @param column
     * @return 
     */
    public String[] getFeedList(int column) {
        String[] retVal = new String[UstadMobileConstants.OPDS_FEEDS_PRESETS.length];
        for(int i = 0; i < retVal.length; i++) {
            retVal[i] = UstadMobileConstants.OPDS_FEEDS_PRESETS[i][column];
        }
        
        return retVal;
    }
    
    public static void addFeedToUserFeedList(String url, String title, String authUser, String authPass, Object context) {
        try {
            JSONArray arr = BasePointController.getUserFeedListArray(context);
            JSONObject newFeed = new JSONObject();
            newFeed.put("url", url);
            newFeed.put("title", title);
            newFeed.put("httpu", authUser);
            newFeed.put("httpp", authPass);
            arr.put(newFeed);
            BasePointController.setUserFeedListArray(arr, context);
        }catch(JSONException e) {
            
        }
    }
    
    public static JSONArray getUserFeedListArray(Object context) {
        JSONArray retVal = null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        try {
            String currentJSON = impl.getUserPref(
                CatalogController.PREFKEY_USERFEEDLIST, null, context);
            if(currentJSON != null) {
                retVal = new JSONArray(currentJSON);
            }else {
                retVal = getDefaultUserFeedList(context);
            }
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 148, null, e);
        }
        
        return retVal;
    }
    
    /**
     * Generates the default user feed list by resolving the DEFAULT_OPDS_SERVER
     * relative to the current xAPI server
     * 
     * @param context
     * @return 
     */
    public static JSONArray getDefaultUserFeedList(Object context) {
        JSONArray retVal = null;
        String xAPIServer = UstadMobileSystemImpl.getInstance().getAppPref(
            UstadMobileSystemImpl.PREFKEY_XAPISERVER, 
            UstadMobileDefaults.DEFAULT_XAPI_SERVER, context);
        try {
            retVal = new JSONArray();
            JSONObject serverFeed = new JSONObject();
            serverFeed.put("title", "Ustad Mobile");
            serverFeed.put("url", UMFileUtil.resolveLink(xAPIServer, 
                UstadMobileDefaults.DEFAULT_OPDS_SERVER));
            serverFeed.put("auth", ":appuser:");
            retVal.put(serverFeed);
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 164, xAPIServer, e);
        }
        
        return retVal;
    }
    
    public static void setUserFeedListArray(JSONArray arr, Object context) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        try {
            impl.setUserPref(CatalogController.PREFKEY_USERFEEDLIST, 
                arr.toString(), context);
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 146, null, e);
        }
    }
    
    public void handleAddFeed(String url, String title) {
        BasePointController.addFeedToUserFeedList(url, title, null, null, context);
        basePointView.refreshCatalog(INDEX_BROWSEFEEDS);
    }
    
    public void handleRemoveItemsFromUserFeed(UstadJSOPDSEntry[] entriesToRemove) {
        if(entriesToRemove.length == 0) {
            return;//nothing to do here
        }
        
        String userPrefix = CatalogController.getUserFeedListIdPrefix(context);
        JSONArray userFeedList = BasePointController.getUserFeedListArray(context);
        JSONArray newUserFeedList = new JSONArray();
        try {
            boolean removeItem;
            String currentID;
            int j;
            
            for(int i = 0; i < userFeedList.length(); i++) {
                removeItem = false;
                currentID = userPrefix + i;
                
                for(j = 0; j < entriesToRemove.length && !removeItem; j++) {
                    if(entriesToRemove[j].id.equals(currentID)) {
                        removeItem = true;
                        break;
                    }
                }
                
                if(!removeItem) {
                    newUserFeedList.put(userFeedList.get(i));
                }
            }
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 144, null, e);
        }
        
        BasePointController.setUserFeedListArray(newUserFeedList, context);
        
        basePointView.refreshCatalog(INDEX_BROWSEFEEDS);
    }
}
