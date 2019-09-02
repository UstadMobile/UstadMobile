package com.ustadmobile.port.android;

import android.Manifest;
import android.content.Context;

import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import com.ustadmobile.sharedse.network.NetworkManagerBleHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import kotlin.ExperimentalStdlibApi;

@ExperimentalStdlibApi
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NetworkManagerBleHelperTest {

    @Rule
    public GrantPermissionRule mPermissionRule  = GrantPermissionRule.grant(
    Manifest.permission.ACCESS_COARSE_LOCATION);

    private static final String NETWORK_SSID = "DIRECT-1s-Android_9d5f";

    private static final String NETWORK_PASSPHRASE = "787jhfhfh$78";
    private NetworkManagerBleHelper managerBleHelper;

    @Before
    public void setUp(){

        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        managerBleHelper = new NetworkManagerBleHelper(context);
        managerBleHelper.setGroupInfo(NETWORK_SSID, NETWORK_PASSPHRASE);
    }


    @RequiresDevice
    @Test
    public void givenConfiguredNetwork_whenRemoveNetworkCalled_shouldRemoveTheNetworkFromTheList(){

        int networkId = managerBleHelper.addNetwork();

        Assert.assertTrue("Network Id generated is not -1", networkId != -1);

        managerBleHelper.deleteTemporaryWifiDirectSsids();

        networkId = managerBleHelper.getNetworkId();

        Assert.assertEquals("Network Id obtained from list is not -1", networkId, -1);

    }
}
