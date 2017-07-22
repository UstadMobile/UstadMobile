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
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.BasePointMenuItem;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.UstadView;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.view.WelcomeView;

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
public class BasePointController extends UstadBaseController implements DialogResultListener{

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
    
    public BasePointController(Object context) {
        super(context);
    }
    
    public static BasePointController makeControllerForView(BasePointView view, Hashtable args, Hashtable savedState) {
        BasePointController ctrl = new BasePointController(view.getContext());
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        if(args == null)
            args = new Hashtable();

        Hashtable defaultArgs = makeDefaultBasePointArgs(view.getContext());
        Enumeration defaultArgsE = defaultArgs.keys();
        Object defaultKey;
        while(defaultArgsE.hasMoreElements()){
            defaultKey = defaultArgsE.nextElement();
            if(!args.containsKey(defaultKey))
                args.put(defaultKey, defaultArgs.get(defaultKey));
        }

        ctrl.args = args;
        ctrl.setView(view);
        view.setClassListVisible(ctrl.isUserTeacher());
        view.setMenuItems(impl.getActiveUser(view.getContext()) != null ?
            CoreBuildConfig.BASEPOINT_MENU_AUTHENTICATED : CoreBuildConfig.BASEPOINT_MENU_GUEST);

        if(savedState != null && savedState.containsKey(ARG_WELCOME_SCREEN_DISPLAYED)){
            ctrl.welcomeScreenDisplayed = savedState.get(ARG_WELCOME_SCREEN_DISPLAYED).toString().equals("true");
        }

        return ctrl;
    }
    
    public static Hashtable makeDefaultBasePointArgs(Object context) {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] basePointURLs = new String[] {CoreBuildConfig.BASEPOINT_CATALOG_URL};
        
        String iPrefix;
        for(int i = 0; i < BasePointController.NUM_CATALOG_TABS; i++) {
            iPrefix = i+BasePointController.OPDS_ARGS_PREFIX;
            args.put(iPrefix + CatalogController.KEY_URL, basePointURLs[i]);
            if(impl.getActiveUser(context) != null) {
                args.put(iPrefix + CatalogController.KEY_HTTPUSER,
                        impl.getActiveUser(context));
            }

            if(impl.getActiveUserAuth(context) != null) {
                args.put(iPrefix + CatalogController.KEY_HTTPPPASS,
                        impl.getActiveUserAuth(context));
            }

            args.put(iPrefix + CatalogController.KEY_FLAGS, 
                new Integer(CatalogController.CACHE_ENABLED));
            args.put(iPrefix + CatalogController.KEY_RESMOD, 
                new Integer(CatalogController.USER_RESOURCE | CatalogController.SHARED_RESOURCE));

            if(CoreBuildConfig.BASEPOINT_FILTER_BY_UI_LANG) {
                args.put(iPrefix + CatalogController.ARG_FILTER_BY_UI_LANG, "true");
            }

            //by default show the browse button on the first tab only
            if(i == 0 && CoreBuildConfig.BASEPOINT_BROWSEBUTTON_ENABLED) {
                args.put(iPrefix + CatalogController.KEY_BROWSE_BUTTON_URL,
                        CoreBuildConfig.BASEPOINT_BROWSEBUTTON_URL);
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
        if(impl.getActiveUser(getContext()) == null)
            return false;

        String classListJSON = impl.getUserPref("teacherclasslist", context);

        if(classListJSON == null) {
            return false;//not a teacher or no classes assigned
        }else {
            return true;
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

    @Override
    public void onDialogResult(int commandId, DismissableDialog dialog, Hashtable args) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        switch(commandId) {
            case LoginController.RESULT_LOGIN_SUCCESSFUL:
                dialog.dismiss();
                basePointView.setMenuItems(CoreBuildConfig.BASEPOINT_MENU_AUTHENTICATED);
                impl.getAppView(getContext()).showNotification("Login successful", AppView.LENGTH_LONG);
                break;
        }
    }

    public void onResume() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(!welcomeScreenDisplayed
                && impl.getAppPref(WelcomeController.PREF_KEY_WELCOME_DONT_SHOW, "false",getContext()).equals("false")) {
            setWelcomeScreenDisplayed(true);
            UstadMobileSystemImpl.getInstance().go(WelcomeView.VIEW_NAME, getContext());
        }
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
}
