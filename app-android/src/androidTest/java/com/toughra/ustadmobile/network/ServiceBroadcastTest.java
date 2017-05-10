package com.toughra.ustadmobile.network;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.impl.http.HTTPService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.port.android.network.NetworkManagerAndroid.EXTRA_SERVICE_NAME;
import static com.ustadmobile.port.android.network.NetworkManagerAndroid.PREF_KEY_SUPERNODE;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by kileha3 on 03/04/2017.
 */

public class ServiceBroadcastTest {

    private static final int mWaitingTimeForBindService =20000;
    private static final int mMaxExecutionTime =180000;
    public static final String TEST_SERVICE_NAME="ustadServiceTest";

    @Rule
    public final ServiceTestRule mServiceRuleWifi = new ServiceTestRule();
    private final ServiceTestRule mServiceRuleHTTPD = new ServiceTestRule();
    private final Object mLock=new Object();
    private NetworkServiceAndroid networkServiceAndroid =null;


    @Before
    public void setUp() throws TimeoutException {
        Context mContext = InstrumentationRegistry.getTargetContext();
        UstadMobileSystemImpl.getInstance().init(mContext);
        UstadMobileSystemImpl.getInstance().setAppPref(PREF_KEY_SUPERNODE, "true", mContext);

        Intent serviceIntent = new Intent(mContext, NetworkServiceAndroid.class);
        serviceIntent.putExtra(EXTRA_SERVICE_NAME,TEST_SERVICE_NAME);
        IBinder mBinder= mServiceRuleWifi.bindService(serviceIntent);
        networkServiceAndroid =((NetworkServiceAndroid.LocalServiceBinder)mBinder) .getService();
        UstadMobileSystemImplAndroid.getInstanceAndroid();

        Intent httpd = new Intent(mContext, HTTPService.class);
        mServiceRuleHTTPD.startService(httpd);



    }


    @Test
    public void testServiceBroadcast() throws InterruptedException {

        synchronized (mLock){
            mLock.wait(mWaitingTimeForBindService);
        }

        assertNotNull("Was service bound: ", networkServiceAndroid);
        WifiDirectHandler mWifiDirectHandler = networkServiceAndroid.getWifiDirectHandlerAPI();


        synchronized (mLock){
            mLock.wait(mMaxExecutionTime);
        }
        assertThat("Was WiFi enabled: ", mWifiDirectHandler.isWifiEnabled(),is(true));
        assertThat("Was Bluetooth enabled: ", networkServiceAndroid.isBluetoothEnabled(),is(true));
        assertThat("Was service added and broadcast: ",
                networkServiceAndroid.getWifiDirectHandlerAPI().getServiceStatus(),
                is(WifiDirectHandler.NORMAL_SERVICE_STATUS_ACTIVE));

    }

}
