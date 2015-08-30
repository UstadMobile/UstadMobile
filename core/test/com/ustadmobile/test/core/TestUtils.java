/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.test.core;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import java.io.IOException;
import java.util.Hashtable;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
    import org.json.*;
/* $endif$ */


/**
 *
 * @author mike
 */
public class TestUtils {
    
    public static TestUtils mainInstance;
    
    private String httpRootDir;
    
    private int serverPort = -1;
    
    public TestUtils() {
        
    }
    
    
    public static TestUtils getInstance() {
        if(mainInstance == null) {
            mainInstance = new TestUtils();
        }
        
        return mainInstance;
    }
    
    public int getHTTPPort() {
        return serverPort;
    }
    
    public String getHTTPRoot() {
        Exception ex = null;
        
        String startServerURL = "http://" + TestConstants.TEST_SERVER + ":"
                    + TestConstants.TEST_CONTROL_PORT + "/?action=newserver";
        if(httpRootDir == null) {
            
                
            try {
                HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(startServerURL,
                    new Hashtable(), new Hashtable(), "GET");
                String serverSays = new String(result.getResponse(), "UTF-8");
                JSONObject response = new JSONObject(serverSays);
                serverPort = response.getInt("port");
                httpRootDir = "http://" + TestConstants.TEST_SERVER + ":" + serverPort + "/";
            }catch(IOException e) {
                System.err.println("Test exception creating new test port");
                e.printStackTrace();
                ex = e;
            }catch(JSONException e) {
                System.err.println("Test exception parsing json");
                e.printStackTrace();
                ex = e;
            }
        }
        
        if(httpRootDir == null ){
            String message = "Cannot create test server: " + startServerURL;
            if(ex != null) {
                message += " " + ex.toString();
            }
            throw new RuntimeException(message);
        }
        
        return httpRootDir;
    }
    
    /**
     * Use the saveresults section of DodgyHTTPD to save results 
     * 
     * @param numPass Number of tests passed
     * @param numFail Number of tests failed
     * @param device Device name performing the tests - used to name the output file
     * @param testlog Complete test log to be sent to the server
     * @throws IOException If an IOException happens communicating with the server
     */
    public void sendResults(int numPass, int numFail, String device, String testlog) throws IOException {
        IOException e = null;
        Hashtable postParams = new Hashtable();
        postParams.put("action", "saveresults");
        postParams.put("numPass", String.valueOf(numPass));
        postParams.put("numFail", String.valueOf(numFail));
        postParams.put("logtext", testlog);
        if(device != null) {
            postParams.put("device", device);
        }
        
        
        
        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(httpRootDir, null, 
            postParams, "POST");
        if(result.getStatus() != 200) {
            throw new IOException("Error sending results to server: status: " 
                + result.getStatus());
        }
        
    }
}
