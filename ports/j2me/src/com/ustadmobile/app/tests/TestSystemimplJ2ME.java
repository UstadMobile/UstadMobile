/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;


import com.ustadmobile.impl.UstadMobileSystemImpl;
import j2meunit.framework.TestCase;

import com.ustadmobile.impl.UstadMobileSystemImplFactory;

/**
 *
 * @author varuna
 */
public class TestSystemimplJ2ME extends TestCase {
    private UstadMobileSystemImpl ustadMobileSystemImpl;
    public TestSystemimplJ2ME(){
        setName("TestSimple Test");
    }
    
    public void runTest() throws Throwable{
        ustadMobileSystemImpl = UstadMobileSystemImplFactory.createUstadSystemImpl();
        assertEquals("Simple Test OK", 2, 1+1);
        String getSharedContentDir = ustadMobileSystemImpl.getSharedContentDir();
        if (getSharedContentDir.toLowerCase().endsWith("ustadmobilecontent/") 
                || getSharedContentDir.toLowerCase().endsWith(
                                        "ustadmobilecontent")){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
       
        
    }
}
