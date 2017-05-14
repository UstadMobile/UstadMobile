package com.ustadmobile.test.port.sharedse.impl;

import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.impl.TestContext;
import com.ustadmobile.test.sharedse.impl.UstadMobileSystemImplTest;
import com.ustadmobile.test.sharedse.network.MockNetworkManager;
import com.ustadmobile.test.sharedse.network.MockRemoteDevice;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class UstadMobileSystemImplTestSE extends UstadMobileSystemImplTest {


    private MockNetworkManager networkManager;

    private MockRemoteDevice testDriver;


    public UstadMobileSystemImplTestSE() {
        super();
        networkManager = new MockNetworkManager(TestConstants.TEST_MOCK_LOCAL_BLUETOOTH_DEVICE);
        testDriver = networkManager.addMockTestDriver(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
    }

    @Override
    public void init(Object context) {
        super.init(context);
        networkManager.init(context, TestConstants.TEST_NETWORK_SERVICE_NAME);
    }

    public MockRemoteDevice getMockTestDriver() {
        return testDriver;
    }


    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }




}
