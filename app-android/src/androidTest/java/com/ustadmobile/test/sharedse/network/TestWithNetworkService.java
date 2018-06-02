package com.ustadmobile.test.sharedse.network;

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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;

import java.util.concurrent.TimeoutException;

/**
 * Created by mike on 3/11/18.
 */

public abstract class TestWithNetworkService {

    @Rule
    public GrantPermissionRule mPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE);


    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();


    private NetworkServiceAndroid sService;

    private ServiceConnection sServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sService = ((NetworkServiceAndroid.LocalServiceBinder)iBinder)
                    .getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Before
    public void bindNetworkService() throws TimeoutException {
        Context context = InstrumentationRegistry.getTargetContext();
        UstadMobileSystemImpl.getInstance().init(context);
        Intent serviceIntent = new Intent(context, NetworkServiceAndroid.class);
        mServiceRule.bindService(serviceIntent, sServiceConnection, Service.BIND_AUTO_CREATE);
    }

    @After
    public void unbindNetworkService() {
        mServiceRule.unbindService();
    }

}
