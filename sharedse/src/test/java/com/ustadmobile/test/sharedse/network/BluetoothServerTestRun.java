package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.test.sharedse.BluetoothServerTestSe;

import org.junit.Before;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class BluetoothServerTestRun extends BluetoothServerTestSe {

    @Before
    public void mockRemoteDevice() {
        MockNetworkManager networkManager = (MockNetworkManager)UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        networkManager.setMockBluetoothServer(BluetoothServerTestSe.REMOTE_BLUETOOTH_MAC, new MockBluetoothServer());
    }

}
