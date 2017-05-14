package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.BluetoothServerTestSe;
import com.ustadmobile.test.sharedse.impl.TestContext;

import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class BluetoothServerTestRun extends BluetoothServerTestSe {

    @BeforeClass
    public static void mockRemoteDevice() {
        MockNetworkManager networkManager = (MockNetworkManager)UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        TestContext mockRemoteContext = new TestContext();
        networkManager.addMockRemoteDevice(networkManager.getKnownNodes().get(0).getDeviceBluetoothMacAddress()
                , mockRemoteContext);
    }

}
