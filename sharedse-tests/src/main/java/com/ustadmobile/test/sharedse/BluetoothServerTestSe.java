package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by mike on 5/10/17.
 */

public class BluetoothServerTestSe implements BluetoothConnectionHandler {

    private boolean connectionCalled = false;

    @Test
    public void testEntryStatus() throws Exception {
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();

        //enable supernode mode on the remote test device
        String enableNodeUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=SUPERNODE&enabled=true";
        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(enableNodeUrl, null, null);
        Assert.assertEquals("Supernode mode reported as enabled", 200, result.getStatus());

        ArrayList<String> entryIds = new ArrayList<>();
        entryIds.add("someid");
        manager.requestFileStatus(entryIds, manager.getContext());
        try { Thread.sleep(240000);}
        catch(InterruptedException e) {}
    }

    //@Test
    public void testBluetoothConnect() throws Exception {
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();

        manager.connectBluetooth(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE, this);
        try { Thread.sleep(5000); }
        catch(InterruptedException e) {}
        Assert.assertTrue("Device connected", connectionCalled);
        connectionCalled = false;


        manager.connectBluetooth(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE +":00:ff", this);
        try { Thread.sleep(5000); }
        catch(InterruptedException e) {}
        Assert.assertTrue("Device not around not connected", !connectionCalled);
    }

    @Override
    public void onConnected(InputStream inputStream, OutputStream outputStream) {
        connectionCalled = true;
    }
}
