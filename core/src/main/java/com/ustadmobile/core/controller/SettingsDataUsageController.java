package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.view.SettingsDataUsageView;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
//import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;

import java.io.IOException;
import java.sql.SQLException;



/**
 * Created by kileha3 on 13/02/2017.
 */

public class SettingsDataUsageController extends UstadBaseController{

    public static final String PREFKEY_SUPERNODE = "supernode_enabled";
    public static final String PREFKEY_CONNECTION_TYPE = "connection_type";
    private static int CONNECTION_TYPE;

    private boolean isSyncHappening = false;

    public static String DEFAULT_MAIN_SERVER_HOST_NAME = "umcloud1svlt";

    private SettingsDataUsageView view = null;

    public SettingsDataUsageController(Object context) {
        super(context);
    }

    public void handleSetSupernodeEnabledChanged(boolean enabled) {

        UstadMobileSystemImpl.getInstance().setAppPref(PREFKEY_SUPERNODE, String.valueOf(enabled), getContext());

        NetworkManagerCore manager = UstadMobileSystemImpl.getInstance().getNetworkManager();

        if(manager != null)
            manager.setSuperNodeEnabled(context, enabled);
    }

    public boolean handleWifiOnlyMode(boolean isWiFiOnly){
        if(isWiFiOnly){
            CONNECTION_TYPE=1;
        }
        UstadMobileSystemImpl.getInstance().setAppPref(PREFKEY_CONNECTION_TYPE, String.valueOf(CONNECTION_TYPE), getContext());
        return isWiFiOnly;
    }

    public boolean handleMobileDataOnlyMode(boolean isMobileData){
        if(isMobileData){
            CONNECTION_TYPE=0;
        }
        UstadMobileSystemImpl.getInstance().setAppPref(PREFKEY_CONNECTION_TYPE,
                String.valueOf(CONNECTION_TYPE), getContext());
        return isMobileData;
    }
    public void setView(UstadView view) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        super.setView(view);
        this.view = (SettingsDataUsageView)view;
        boolean wifiP2PSupported = impl.isWiFiP2PSupported();
        this.view.setSupernodeSettingVisible(wifiP2PSupported);

        /* $if umplatform != 2 $ */
        if(wifiP2PSupported) {
            this.view.setSupernodeEnabled(Boolean.parseBoolean(impl.getAppPref(
                    PREFKEY_SUPERNODE, "false", getContext())));
        }
        /* $endif$ */
    }

    public void setUIStrings() {
    }


    public void triggerSync() throws Exception {

        new Thread(new Runnable() {
            @Override
            public void run() {
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
                NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);
                String loggedInUsername = impl.getActiveUser(getContext());
                User loggedInUser = userManager.findByUsername(getContext(), loggedInUsername);
                String loggedInUserCred = impl.getActiveUserAuth(getContext());
                Node endNode = null;
                try {
                    endNode = nodeManager.getMainNode(DEFAULT_MAIN_SERVER_HOST_NAME, context);
                    UMSyncResult result = UMSyncEndpoint.startSync(loggedInUser, loggedInUserCred,
                            endNode, getContext());
                    if(result.getStatus() > -1){
                        //UstadMobileSystemImplSE.getInstanceSE().fireSetSyncHappeningEvent(true, context);
                        UstadMobileSystemImpl.getInstance().fireSetSyncHappeningEvent(false, context);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //Update view that something went wrong TODO
                }

            }
        }).start();

    }
}
