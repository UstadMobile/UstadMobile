package com.ustadmobile.test.sharedse;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mike on 5/10/17.
 */

public class BluetoothServerTestSe implements BluetoothConnectionHandler {

    private boolean connectionCalled = false;
    private static final String ENTRY_ID="31daeq7-617d-402e-a0b0-dba52ef21911";

    @Test
    public void testBluetoothConnect() throws Exception {
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assume.assumeTrue("Network test wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());

        manager.connectBluetooth(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE, this);
        try { Thread.sleep(5000); }
        catch(InterruptedException e) {}
        Assert.assertTrue("Device connected", connectionCalled);
        connectionCalled = false;


        manager.connectBluetooth(manager.getKnownNodes().get(0).getDeviceBluetoothMacAddress(), this);
        try { Thread.sleep(5000); }
        catch(InterruptedException e) {}
        Assert.assertTrue("Device not around not connected", !connectionCalled);
    }

    @Override
    public void onConnected(InputStream inputStream, OutputStream outputStream) {
        connectionCalled = true;
    }
}
