package com.ustadmobile.test.port.sharedse;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.network.TestWifiDirectGroupConnection;

import org.junit.BeforeClass;

/**
 * Created by mike on 6/2/17.
 */

public abstract class TestWifiDirectGroupConnectionRunTmp extends TestWifiDirectGroupConnection {

    @BeforeClass
    public static void initSystemImpl(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getInstance().init(PlatformTestUtil.getTargetContext());
    }
}
