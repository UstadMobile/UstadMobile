/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.HTTPUtils;
import com.ustadmobile.controller.CatalogController;
import com.ustadmobile.impl.HTTPResult;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import j2meunit.framework.TestCase;

/**
 *
 * @author varuna
 */
public class TestHttpResult extends TestCase {
    public TestHttpResult(){
        setName("TestHTTPResult Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("Simple Test OK", 2, 1+1);
        String server = 
                UstadMobileSystemImpl.getInstance().getAppPref("server");
        HTTPResult httpResult = null;
        httpResult = HTTPUtils.makeHTTPRequest(
                server, null, null, "GET");
        int x = 0;
        byte[] responseByte = httpResult.getResponse();
        String response = new String(responseByte);
        assertEquals("response", "X-Experience-API-Version header missing",
                response);
        
        String opdsEndpoint = 
                UstadMobileSystemImpl.getInstance().getAppPref("opds");
        opdsEndpoint = FileUtils.joinPath(opdsEndpoint, "assigned_courses") +
                FileUtils.FILE_SEP; 
                
        CatalogController catalogController = 
                CatalogController.makeControllerByURL(opdsEndpoint, null);
        catalogController.show();
        
        Thread.sleep(2000);
        
    }
}
