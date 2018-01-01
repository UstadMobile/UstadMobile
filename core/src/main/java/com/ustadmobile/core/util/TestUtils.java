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
    


    
}
