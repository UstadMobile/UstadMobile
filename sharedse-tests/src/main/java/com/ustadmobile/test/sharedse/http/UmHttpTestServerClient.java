package com.ustadmobile.test.sharedse.http;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.URLTextUtil;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class UmHttpTestServerClient {

    public static int newServer() {
        return newServer(TestConstants.UM_HTTP_TESTSERVER_HOSTNAME,
                TestConstants.UM_HTTP_TESTSERVER_CONTROL_PORT, PlatformTestUtil.getTargetContext());
    }

    public static final int newServer(String controlHost, int controlPort, Object context) {
        try {
            String httpUrl = "http://" + controlHost + ":" + controlPort + "/?cmd=new";
            UmHttpRequest request = new UmHttpRequest(context, httpUrl);
            UmHttpResponse response = UstadMobileSystemImpl.getInstance().makeRequestSync(request);
            JSONObject jsonResponse = new JSONObject(UMIOUtils.readStreamToString(response.getResponseAsStream()));
            int newPort = jsonResponse.getInt("port");
            UstadMobileSystemImpl.l(UMLog.INFO, 0,
                    "UmHttpTestServerClient: started new server on port" + newPort);
            return newPort;
        }catch(IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static JSONObject sendCommand(String controlHost, int controlPort,
                                         String command, int serverPort,
                                         Map<String, String> parameters,
                                         Object context) {
        try {
            String httpUrl = "http://" + controlHost + ":" + controlPort + "?cmd=" + command +
                    "&port=" + serverPort;
            if(parameters != null && !parameters.isEmpty()) {
                for(String paramName : parameters.keySet()) {
                    httpUrl += "&" + URLTextUtil.urlEncodeUTF8(paramName) + "=" +
                            URLTextUtil.urlEncodeUTF8(parameters.get(paramName));
                }
            }

            UmHttpRequest request = new UmHttpRequest(context, httpUrl);
            UmHttpResponse response = UstadMobileSystemImpl.getInstance().makeRequestSync(request);
            if(!response.isSuccessful()) {
                throw new RuntimeException("Send command :" + command + " to " + httpUrl +
                        " failed with response code " + response.getStatus());
            }else if(response.isSuccessful() && response.getStatus() == 204) {
                UstadMobileSystemImpl.l(UMLog.INFO, 0, "UmHttpTestServerClient: " +
                    " 204 OK sent command: " + command + " for server on port " + serverPort);
                return null;//no response, but was successful
            }else{
                UstadMobileSystemImpl.l(UMLog.INFO, 0, "UmHttpTestServerClient: " +
                        " 200 OK sent command: " + command + " for server on port " + serverPort);
                JSONObject jsonResponse = new JSONObject(UMIOUtils.readStreamToString(
                        response.getResponseAsStream()));
                return jsonResponse;
            }
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 0, "UmHttpTestServerClient: " +
                    " IOException sending command");
            throw new RuntimeException(e);
        }
    }

    public static JSONObject sendCommand(String command, int serverPort,
                                         Map<String, String> parameters, Object context) {
        return sendCommand(TestConstants.UM_HTTP_TESTSERVER_HOSTNAME,
                TestConstants.UM_HTTP_TESTSERVER_CONTROL_PORT, command, serverPort, parameters,
                context);
    }

    public static String getServerBasePath(int port) {
        return "http://" + TestConstants.UM_HTTP_TESTSERVER_HOSTNAME + ":" + port + "/";
    }

    public static HashMap<String, String> throttleParams(long bytesPerSecond) {
        HashMap<String, String> params = new HashMap<>();
        params.put("bytespersecond", String.valueOf(bytesPerSecond));
        return params;
    }

    public static boolean throttleServer(int serverPort, long bytesPerSecond, Object context) {
        try {
            sendCommand("throttle", serverPort, throttleParams(bytesPerSecond), context);
            return true;
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
