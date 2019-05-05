/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.controller;


import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.BasePointMenuItem;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.UstadView;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * The base point is right now basically a wrapper containing two catalogs
 * 
 * 
 * @author mike
 */
public class BasePointController extends UstadBaseController {

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

    private boolean welcomeScreenDisplayed = false;

    public static final String ARG_WELCOME_SCREEN_DISPLAYED = "wsd";

    private boolean keepTmpVariables = false;

    public static final int CMD_SHARE_APP=1005;

    public static final int CMD_RECEIVE_ENTRY = 1006;

    public BasePointController(Object context, BasePointView view) {
        super(context);
        this.basePointView = view;
    }

    public void onCreate(Hashtable args, Hashtable savedState) {
        this.args = args;
        basePointView.setClassListVisible(false);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        if(savedState != null && savedState.containsKey(ARG_WELCOME_SCREEN_DISPLAYED)){
            welcomeScreenDisplayed = savedState.get(ARG_WELCOME_SCREEN_DISPLAYED).toString().equals("true");
        }

        Vector catalogTabs = null;
        if(args != null) {
            catalogTabs = UMFileUtil.splitCombinedViewArguments(args, "catalog", '-');
        }

        if(catalogTabs == null || catalogTabs.isEmpty()) {
            String defaultArgs = UstadMobileSystemImpl.getInstance().getAppConfigString(AppConfig.KEY_FIRST_DEST,
                    null, getContext());
            catalogTabs = UMFileUtil.splitCombinedViewArguments(UMFileUtil.parseURLQueryString(defaultArgs),
                    "catalog", '-');
        }

        for(int i = 0; i < catalogTabs.size(); i++) {
            basePointView.addTab((Hashtable)catalogTabs.elementAt(i));
        }
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


    /**
     * Handle when the user clicks one of the base point menu items.
     *
     * This is configured via the buildconfig system : see buildconfig.default.properties for
     * details on configuring this.
     *
     * @param item
     */
    public void handleClickBasePointMenuItem(BasePointMenuItem item) {
        UstadMobileSystemImpl.getInstance().go(item.getDestination(), getContext());
    }

    public void onResume() {

    }

    public boolean isWelcomeScreenDisplayed() {
        return welcomeScreenDisplayed;
    }

    public void setWelcomeScreenDisplayed(boolean welcomeScreenDisplayed) {
        this.welcomeScreenDisplayed = welcomeScreenDisplayed;
    }

    public void onDestroy() {
        if(!keepTmpVariables) {
            UstadMobileSystemImpl.getInstance().setAppPref("tmp" + ARG_WELCOME_SCREEN_DISPLAYED,
                    null, getContext());
        }
    }

    public void handleClickShareApp() {
        basePointView.showShareAppDialog();
    }

    public void handleClickReceive() {
        UstadMobileSystemImpl.getInstance().go("ReceiveCourse", null, getContext());
    }

    public void handleClickConfirmShareApp(final boolean zip) {

    }


}
