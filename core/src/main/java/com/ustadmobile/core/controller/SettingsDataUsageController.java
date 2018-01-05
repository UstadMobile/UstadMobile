package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.view.SettingsDataUsageView;
import com.ustadmobile.core.view.UstadView;

/**
 * Created by kileha3 on 13/02/2017.
 */

public class SettingsDataUsageController extends UstadBaseController{

    public static final String PREFKEY_SUPERNODE = "supernode_enabled";
    public static final String PREFKEY_CONNECTION_TYPE = "connection_type";
    private static int CONNECTION_TYPE;

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
                try {
                    impl.triggerSync(getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
