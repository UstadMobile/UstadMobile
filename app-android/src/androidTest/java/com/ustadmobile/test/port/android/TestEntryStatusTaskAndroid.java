package com.ustadmobile.test.port.android;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.test.sharedse.TestEntryStatusTask;

import org.junit.BeforeClass;

/**
 * Created by kileha3 on 16/05/2017.
 */

public class TestEntryStatusTaskAndroid  extends TestEntryStatusTask {
    public static final ServiceTestRule mServiceRule = new ServiceTestRule();

    @BeforeClass
    public static void startNetworkService() throws Exception{
        Context context = InstrumentationRegistry.getTargetContext();
        Intent serviceIntent = new Intent(context, NetworkServiceAndroid.class);
        mServiceRule.bindService(serviceIntent);
    }


}
