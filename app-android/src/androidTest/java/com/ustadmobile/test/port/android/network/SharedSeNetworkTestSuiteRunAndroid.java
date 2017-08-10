package com.ustadmobile.test.port.android.network;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.test.sharedse.network.SharedSeNetworkTestSuite;

import org.junit.BeforeClass;

/**
 * Created by mike on 8/9/17.
 */

public class SharedSeNetworkTestSuiteRunAndroid extends SharedSeNetworkTestSuite {

    public static final ServiceTestRule mServiceRule = new ServiceTestRule();

    @BeforeClass
    public static void startNetworkService() throws Exception{
        Context context = InstrumentationRegistry.getTargetContext();
        UstadMobileSystemImpl.getInstance().init(context);
        Intent serviceIntent = new Intent(context, NetworkServiceAndroid.class);
        mServiceRule.bindService(serviceIntent);
    }

}
