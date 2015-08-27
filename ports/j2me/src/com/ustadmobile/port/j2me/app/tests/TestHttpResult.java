/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.app.tests;

import com.ustadmobile.port.j2me.app.FileUtils;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.UstadMobileSystemImplFactory;
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
        UstadMobileSystemImpl impl = 
                UstadMobileSystemImplFactory.createUstadSystemImpl();
        CatalogController catalogController =
            CatalogController.makeControllerByURL(opdsEndpoint, impl, 
                CatalogController.USER_RESOURCE, "karmakid02", "karmakid02", 0);
        catalogController.show();
        
        Thread.sleep(2000);
        
    }
}
