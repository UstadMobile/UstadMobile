/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.util;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
/* $endif$ */


/**
 *
 * @author mike
 */
public class TestUtils {
    
    public static TestUtils mainInstance;
    
    private String httpRootDir;
    
    private int serverPort = -1;
    
    public static final String PROP_TESTSERVER = "ustadmobile.testserver";
    
    public static final String PROP_TESTPORT = "ustadmobile.testport";
    
    public static final String PROP_TESTUSER = "ustadmobile.testuser";
    
    public static final String PROP_TESTAUTH = "ustadmobile.testauth";

    public static final int DEFAULT_NETWORK_TIMEOUT = 20000;

    public static final int DEFAULT_NETWORK_INTERVAL = 1000;

    
    public TestUtils() {
        
    }
    
    
    public static TestUtils getInstance() {
        if(mainInstance == null) {
            mainInstance = new TestUtils();
        }
        
        return mainInstance;
    }
    
    public String getTestProperty(String propName) {
        return System.getProperty(propName);
    }
    
    
    public int getHTTPPort() {
        return serverPort;
    }
    
    /**
     * Set the speed limits and force error parameters on the testing HTTP server
     * 
     * @param speedLimit Speed limit in bytes per second or 0 for no limit
     * @param forceErrorAfter Number of bytes after which an error will be forced (e.g. test resume handling)
     * @return true if set OK, false otherwise
     */
    public boolean setLimits(int speedLimit, int forceErrorAfter) {
        String setParamsURL = "http://" + System.getProperty(PROP_TESTSERVER) + ":"
                + System.getProperty(PROP_TESTPORT) + "/?action=setparams&port=" + serverPort
                + "&speedlimit=" + speedLimit + "&forceerrorafter=" + forceErrorAfter;
        boolean setValues = false;
        try {
            HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(setParamsURL,
                new Hashtable(), new Hashtable(), "GET");
            setValues = result.getStatus() == 200;
        }catch(IOException e) {
            UstadMobileSystemImpl.getInstance().l(UMLog.ERROR, 118, setParamsURL, e);
        }
        return setValues;
    }
    
    /**
     * Get the current HTTP Root directory - includes a trailing /
     * 
     * @return HTTP Asset root directory with a trailing /
     */
    public String getHTTPRoot() {
        Exception ex = null;
                
        String startServerURL = "http://" + System.getProperty(PROP_TESTSERVER) + ":"
                    + System.getProperty(PROP_TESTPORT) + "/?action=newserver";
        if(httpRootDir == null) {
            try {
                HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(startServerURL,
                    new Hashtable(), new Hashtable(), "GET");
                String serverSays = new String(result.getResponse(), "UTF-8");
                JSONObject response = new JSONObject(serverSays);
                serverPort = response.getInt("port");
                httpRootDir = "http://" + System.getProperty(PROP_TESTSERVER) + ":" + serverPort + "/";
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
        
        Hashtable headers = new Hashtable();
        headers.put("Connection", "close");
        
        
        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(
                "http://" + System.getProperty(PROP_TESTSERVER) + ":"
                    + System.getProperty(PROP_TESTPORT) + "/", 
                headers, postParams, "POST");
        if(result.getStatus() != 200) {
            throw new IOException("Error sending results to server: status: " 
                + result.getStatus());
        }
        
    }
    
    /**
     * This can be used to avoid putting network code in the main thread
     * 
     * Simply start a thread that puts the value in the hashtable and
     * then call this method with the key that we should wait for.  Hashtable
     * is thread safe...
     * 
     * @param valKey
     * @param table 
     */
    public static void waitForValueInTable(String valKey, Hashtable table) {
        int t = 0;
        for(t = DEFAULT_NETWORK_TIMEOUT; t > 0 && table.get(valKey) == null; t -= DEFAULT_NETWORK_INTERVAL) {
            try { Thread.sleep(DEFAULT_NETWORK_INTERVAL); }
            catch(InterruptedException e) {}
        }
    }
    
}
