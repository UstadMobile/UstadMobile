/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.ustadmobile.app.controller.UstadMobileAppController;
import j2meunit.framework.TestCase;
import java.util.Hashtable;
import com.ustadmobile.app.SerializedHashtable;
import java.util.Enumeration;

/**
 *
 * @author varuna
 */
public class TestSerializedHashtable extends TestCase{

    public TestSerializedHashtable() {
    }
 
    public void runTest() throws Throwable{

        //We get a hash table!
        UstadMobileAppController.setDefaultAppSettings();
        Hashtable defaultSettings = UstadMobileAppController.appSettings;
        byte[] byteArray = SerializedHashtable.hashTabletoStream(defaultSettings);
        Hashtable serialisedHashtable = SerializedHashtable.streamToHashtable(byteArray);
        
        Enumeration e = defaultSettings.elements();
        while (e.hasMoreElements()){
            String key = (String) e.nextElement();
            assertEquals("Serialised Hashtable tests", defaultSettings.get(key),
                    serialisedHashtable.get(key));
        }

    }
    
}
