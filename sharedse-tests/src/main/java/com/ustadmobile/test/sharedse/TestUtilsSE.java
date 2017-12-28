package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.URLTextUtil;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

    public static boolean setRemoteTestMangleWifi(boolean enabled) throws IOException {
        return sendBooleanCommand(RemoteTestServerHttpd.CMD_MANGLE_WIFI_DIRECT_GROUP, enabled);
    }

    public static boolean disableRemoteWifi(int duration) {
        String url = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=" + RemoteTestServerHttpd.CMD_DISABLE_WIFI
                + "&duration=" + duration;
        return sendCommand(url);
    }

    public static boolean requestSendFileViaWifiDirect(String macAddr, String[] entryIds) {
        String commandUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=" + RemoteTestServerHttpd.CMD_SEND_COURSE +
                "&dst=" + URLTextUtil.urlEncode(macAddr, "UTF-8");
        for(int i = 0; i < entryIds.length; i++) {
            commandUrl += "&entryId=" + URLTextUtil.urlEncodeUTF8(entryIds[i]);
        }
        return sendCommand(commandUrl);
    }

    private static boolean sendCommand(String url) {
        int status = -1;
        for(int i = 0; status != 200 && i < 4; i++) {
            InputStream in = null;
            ByteArrayOutputStream bout = null;

            try {
                HttpURLConnection urlConnection = (HttpURLConnection)new URL(url).openConnection();
                status = urlConnection.getResponseCode();
                in = urlConnection.getInputStream();
                bout = new ByteArrayOutputStream();
                UMIOUtils.readFully(in, bout);
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 79, "Error sending command to "
                        + url + "(Attempt #" + (i+1) + ")");
                try { Thread.sleep(1000); }
                catch(InterruptedException e2) {}
            }
        }

        boolean successful = status == 200;
        UstadMobileSystemImpl.l(UMLog.DEBUG, 659, "Send command to ./" + url + " successful ? " + successful);
        return successful;
    }

    private static boolean sendBooleanCommand(String command, boolean value) {
        String url = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=" + command + "&enabled="
                + String.valueOf(value);
        return sendCommand(url);
    }

}
