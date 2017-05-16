package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static com.ustadmobile.test.core.buildconfig.TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT;

/**
 * Created by kileha3 on 16/05/2017.
 */

public class TestNetworkManager {

    @Test
    public void testDiscovery() throws IOException{
        NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();

        //enable supernode mode on the remote test device
        String enableNodeUrl = "http://"+ PlatformTestUtil.getRemoteTestEndpoint() +":"+ TEST_REMOTE_SLAVE_SERVER_PORT + "/?cmd=SUPERNODE&enabled=true";
        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(enableNodeUrl, null, null);
        Assert.assertEquals("Supernode mode reported as enabled", 200, result.getStatus());

        try { Thread.sleep(60000); }
        catch(InterruptedException e ) {}
        Assert.assertNotNull("Remote test slave node discovered",
                manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE));
    }
}
