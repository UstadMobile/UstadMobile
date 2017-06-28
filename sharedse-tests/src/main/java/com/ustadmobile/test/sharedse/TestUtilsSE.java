package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;

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
        return sendBooleanCommand(RemoteTestServerHttpd.CMD_SETSUPERNODE_ENABLED, enabled);
    }

    public static boolean setRemoteTestMangleBluetooth(boolean enabled) throws IOException {
        return sendBooleanCommand(RemoteTestServerHttpd.CMD_MANGLE_BLUETOOTH, enabled);
    }

    private static boolean sendBooleanCommand(String command, boolean value) throws IOException{
        String url = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=" + command + "&enabled="
                + String.valueOf(value);
        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(url, null, null);
        return result.getStatus() == 200;
    }

}
