package com.ustadmobile.test.sharedse.impl;

import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import java.util.Properties;

/**
 * Created by mike on 5/13/17.
 */

public class TestContext {

    private Properties appProps;

    private Properties userPrefs;

    private NetworkManager networkManager;


    public TestContext() {
        appProps = new Properties();
        userPrefs = new Properties();
    }


    public Properties getAppProps() {
        return appProps;
    }

    public Properties getUserPrefs() {
        return userPrefs;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }
}
