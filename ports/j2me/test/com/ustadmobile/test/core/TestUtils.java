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

/* $if umplatform == 2  $ */
    import org.json.*;
 /* $else$
    import org.json.*;
$endif$ */


/**
 *
 * @author mike
 */
public class TestUtils {
    
    public static TestUtils mainInstance;
    
    private String httpRootDir;
    
    public TestUtils() {
        
    }
    
    
    public static TestUtils getInstance() {
        if(mainInstance == null) {
            mainInstance = new TestUtils();
        }
        
        return mainInstance;
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
                int serverPort = response.getInt("port");
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
}
