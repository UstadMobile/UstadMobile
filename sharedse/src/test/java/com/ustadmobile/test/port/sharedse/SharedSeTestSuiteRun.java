package com.ustadmobile.test.port.sharedse;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.SharedSeTestSuite;
import com.ustadmobile.test.sharedse.network.MockBluetoothServer;
import com.ustadmobile.test.sharedse.network.MockNetworkManager;

import org.junit.BeforeClass;

/**
 * Created by mike on 5/10/17.
 */

public class SharedSeTestSuiteRun extends SharedSeTestSuite{

    @BeforeClass
    public static void setupMockNetwork() {
        MockNetworkManager testNetworkManager = (MockNetworkManager)UstadMobileSystemImplSE.getInstance().getNetworkManager();
        testNetworkManager.setMockBluetoothServer(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
            new MockBluetoothServer());
    }
}
