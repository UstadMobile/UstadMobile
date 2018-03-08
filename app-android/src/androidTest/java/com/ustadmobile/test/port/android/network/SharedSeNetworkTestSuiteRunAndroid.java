package com.ustadmobile.test.port.android.network;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.rule.ServiceTestRule;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.test.sharedse.network.SharedSeNetworkTestSuite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

/**
 * Created by mike on 8/9/17.
 */
public class SharedSeNetworkTestSuiteRunAndroid extends SharedSeNetworkTestSuite {

    public static final ServiceTestRule mServiceRule = new ServiceTestRule();

    private static final Object p2pActiveLockObj = new Object();

    private static NetworkServiceAndroid sService;

    public static ServiceConnection sServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sService = ((NetworkServiceAndroid.LocalServiceBinder)iBinder)
                    .getService();
            sService.getNetworkManager().addActiveP2pNetworkObject(p2pActiveLockObj);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    /*
     * See also: grant permissions for the test application id using the shell e.g.
     *
     * Warning: these don't all work because of a bug in the testing library:
     *  https://issuetracker.google.com/issues/64389280
     *
     *  So you still have to run:
     *
     * $ adb shell pm grant com.toughra.ustadmobile.uswdp.test android.permission.ACCESS_COARSE_LOCATION
     * $ adb shell pm grant com.toughra.ustadmobile.uswdp.test android.permission.ACCESS_FINE_LOCATION
     */
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);



    @BeforeClass
    public static void startNetworkService() throws Exception{
        Context context = InstrumentationRegistry.getTargetContext();
        UstadMobileSystemImpl.getInstance().init(context);
        Intent serviceIntent = new Intent(context, NetworkServiceAndroid.class);
        mServiceRule.bindService(serviceIntent, sServiceConnection, Service.BIND_AUTO_CREATE);
    }

    @AfterClass
    public static void stopNetworkService() throws Exception {
        sService.getNetworkManager().removeActiveP2pNetworkObject(p2pActiveLockObj);
        mServiceRule.unbindService();
    }

}
