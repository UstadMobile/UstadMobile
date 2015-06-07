/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import j2meunit.framework.TestCase;

/**
 *
 * @author varuna
 */
public class TestXmlParse extends TestCase {
    
    public TestXmlParse(){
        setName("Testing XML Parse");
    }
    
    public void runTest() throws Throwable{
    
        if (TestUtils.testSettings.get("appDataURI").toString() != null ){
            assertTrue("Hashtable get appDataURI", true);
        }
        
        assertEquals("Hashtable XML Parse test-settings.xml", "test.opds",
                TestUtils.getInstance().testSettings.get("opdsxml"));
        assertEquals("Hashtable XML Parse test-settings.xml OPF", "test.opf",
                TestUtils.getInstance().testSettings.get("opfxml"));
        assertEquals("Hashtable XML Parse test-settings.xml username", "mike", 
                TestUtils.getInstance().testSettings.get("username"));
        assertEquals("Hashtable XML Parse test-settings.xml password", "secret",
                TestUtils.getInstance().testSettings.get("password"));
        
        
        
    }
    
}
