package com.ustadmobile.test.port.sharedse.network;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.network.SharedSeNetworkTestSuite;

import org.junit.BeforeClass;

/**
 * Created by mike on 8/9/17.
 */

public class SharedSeNetworkTestSuiteRun extends SharedSeNetworkTestSuite {

    @BeforeClass
    public static void initSystemImpl(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getInstance().init(PlatformTestUtil.getTargetContext());
    }

}
