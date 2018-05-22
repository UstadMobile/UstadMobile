package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.annotation.PeerServerRequiredTest;

import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by mike on 8/9/17.
 */
@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestNetworkManager.class,
//        TestWifiDirectSendReceive.class,
//        TestEntryStatusTask.class,
//        TestNetworkManagerEntryStatusMonitoring.class,
//        TestDownloadTask.class,
//        TestWifiDirectGroupConnection.class
})
@PeerServerRequiredTest
public class SharedSeNetworkTestSuite {


    /**
     * When the tests run on an emulator (e.g. Jenkins) we must run an assume statement that the
     * network hardware is present to ensure that tests are skipped when such hardware is not
     * present.
     */
    public static void assumeNetworkHardwareEnabled() {
        final NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assume.assumeTrue("Bluetooth enabled", manager.isBluetoothEnabled());
        Assume.assumeTrue("Wifi enabled", manager.isWiFiEnabled());
    }

}
