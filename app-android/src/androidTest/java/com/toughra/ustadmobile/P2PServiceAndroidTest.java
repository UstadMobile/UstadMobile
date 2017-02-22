package com.toughra.ustadmobile;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.port.android.p2p.P2PServiceAndroid;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * Created by kileha3 on 09/02/2017.
 */

@RunWith(AndroidJUnit4.class)

public class P2PServiceAndroidTest{

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testService() throws TimeoutException {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), P2PServiceAndroid.class);
        IBinder binder = mServiceRule.bindService(serviceIntent);

        P2PServiceAndroid service = ((P2PServiceAndroid.LocalServiceBinder) binder).getService();
        assertEquals(service.isRunning(), false);

    }
}
