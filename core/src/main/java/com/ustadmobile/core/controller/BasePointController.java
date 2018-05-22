/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.controller;


import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.BasePointMenuItem;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.core.view.WelcomeView;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
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

        basePointView.setMenuItems(impl.getActiveUser(getContext()) != null ?
                CoreBuildConfig.BASEPOINT_MENU_AUTHENTICATED : CoreBuildConfig.BASEPOINT_MENU_GUEST);

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

    public void onDialogResult(int commandId, DismissableDialog dialog, Hashtable args) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        switch(commandId) {
            case LoginController.RESULT_LOGIN_SUCCESSFUL:
                dialog.dismiss();
                basePointView.setMenuItems(CoreBuildConfig.BASEPOINT_MENU_AUTHENTICATED);
                impl.getAppView(getContext()).showNotification(
                        impl.getString(MessageID.login_successful, getContext()),
                        AppView.LENGTH_LONG);
                break;

            case RegistrationPresenter.RESULT_REGISTRATION_SUCCESS:
                dialog.dismiss();
                basePointView.setMenuItems(CoreBuildConfig.BASEPOINT_MENU_AUTHENTICATED);
                impl.getAppView(getContext()).showNotification(
                        impl.getString(MessageID.registration_successful, getContext()),
                        AppView.LENGTH_LONG);
                break;
        }
    }

    public void onResume() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(CoreBuildConfig.WELCOME_DIALOG_ENABLED && !welcomeScreenDisplayed
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

    public void handleClickShareApp() {
        basePointView.showShareAppDialog();
    }

    public void handleClickReceive() {
        UstadMobileSystemImpl.getInstance().go("ReceiveCourse", null, getContext());
    }

    public void handleClickConfirmShareApp(final boolean zip) {
        final UstadMobileSystemImpl impl =UstadMobileSystemImpl.getInstance();
        basePointView.setShareAppDialogProgressVisible(true);
        impl.getAppSetupFile(getContext(), zip, new UmCallback() {

            @Override
            public void onSuccess(Object result) {
                impl.getNetworkManager().shareAppSetupFile((String)result,
                        impl.getString(MessageID.share, getContext()));
                basePointView.dismissShareAppDialog();
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }


}
