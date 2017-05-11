package com.ustadmobile.test.port.sharedse.impl;

import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.sharedse.impl.UstadMobileSystemImplTest;
import com.ustadmobile.test.sharedse.network.NetworkManagerTest;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class UstadMobileSystemImplTestSE extends UstadMobileSystemImplTest {


    private NetworkManagerTest networkManager;

    public UstadMobileSystemImplTestSE() {
        super();
        networkManager = new NetworkManagerTest();
        //networkManager.init();
    }

    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }
}
