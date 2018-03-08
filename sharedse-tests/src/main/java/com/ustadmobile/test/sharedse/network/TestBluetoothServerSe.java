package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.annotation.PeerServerRequiredTest;
import com.ustadmobile.test.core.buildconfig.TestConstants;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;

import static com.ustadmobile.test.sharedse.network.TestNetworkManager.NODE_DISCOVERY_TIMEOUT;

/**
 * Created by mike on 5/10/17.
 */
@PeerServerRequiredTest
public class TestBluetoothServerSe implements BluetoothConnectionHandler {

    private boolean isConnectionCalled = false;
    private final Object bluetoothLock=new Object();
    private final Object bluetoothNodeLock =new Object();
    @Test
    public void testBluetoothConnect() throws Exception {
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assume.assumeTrue("Network test wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());

        manager.connectBluetooth(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE, this);
        synchronized (bluetoothLock){
            bluetoothLock.wait(NODE_DISCOVERY_TIMEOUT);
        }
        Assert.assertTrue("Device connected", isConnectionCalled);
        isConnectionCalled = false;

        synchronized (bluetoothNodeLock){
            bluetoothNodeLock.wait(NODE_DISCOVERY_TIMEOUT);
        }
        manager.connectBluetooth(manager.getKnownNodes().get(0).getBluetoothMacAddress(), this);
        Assert.assertTrue("Device not around not connected", !isConnectionCalled);
    }

    @Override
    public void onBluetoothConnected(InputStream inputStream, OutputStream outputStream) {
        isConnectionCalled = true;
       synchronized (bluetoothLock){
           bluetoothLock.notify();
       }

       synchronized (bluetoothNodeLock){
           bluetoothNodeLock.notify();
       }
    }

    @Override
    public void onBluetoothConnectionFailed(Exception exception) {

    }
}
