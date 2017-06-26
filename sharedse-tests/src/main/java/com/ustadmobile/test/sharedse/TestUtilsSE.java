package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import java.io.IOException;

/**
 * Collection of utility methods used for testing
 *
 */
public class TestUtilsSE {

    /**
     * Enable or disable supernode mode on the remote test slave
     *
     * @param enabled True to set enabled, false otherwise
     *
     * @return True if successful, false otherwise
     */
    public static boolean setRemoteTestSlaveSupernodeEnabled(boolean enabled) throws IOException{
        String enableNodeUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=SUPERNODE&enabled="
                + String.valueOf(enabled);
        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(enableNodeUrl, null, null);
        return result.getStatus() == 200;
    }

}
