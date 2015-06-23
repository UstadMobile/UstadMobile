/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.ustadmobile.app.AppPref;
import com.ustadmobile.app.FileUtils;
import j2meunit.framework.TestCase;
import java.util.Hashtable;

/**
 *
 * @author varuna
 */
public class TestAppPref extends TestCase {
    public TestAppPref(){
        setName("TestAppPref Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("Simple Test OK", 2, 1+1);
        
        AppPref appPreferences = new AppPref();
        
        //Set up app settings..
        Hashtable appSettings = appPreferences.getAppSettings();
        
        assertEquals("app settings test", "http://umcloud1.ustadmobile.com", 
                appSettings.get("umcloud"));
        
        //Update settings
        appPreferences.updateSetting("umcloud", 
                "http://umcloud1.ustadmobile.com/");
        
        appSettings.clear();
        appSettings = appPreferences.getAppSettings();
        
        assertEquals("app settings update test", 
                "http://umcloud1.ustadmobile.com/", appSettings.get("umcloud"));
        
        //Get all keys
        String [] prefKeys = 
                FileUtils.enumerationToStringArray(appSettings.keys());
        boolean found=false;
        for (int i=0; i<prefKeys.length; i++){
            if(prefKeys[i].indexOf("tincan") >=0 ){
                found=true;
                break;
            }
        }
        if (found){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        //Add a setting
        appPreferences.addSetting("newkey", "newvalue");
        
        //Remove a setting
        appPreferences.deleteSetting("newkey");
    }
}
