/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.ustadmobile.app.FileUtils;
import j2meunit.framework.TestCase;
import java.util.Hashtable;
import com.ustadmobile.app.UserPref;

/**
 *
 * @author varuna
 */
public class TestUserPref extends TestCase {
    public TestUserPref(){
        setName("User Preferences Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("Simple Test OK", 2, 1+1);
        UserPref userPreferences = new UserPref();
        
        //Set up app settings..
        Hashtable userSettings = userPreferences.getUserSettings();
        
        
        //assertEquals("app settings test", "", 
        //        userSettings.get("username"));
        
        //Update settings
        userPreferences.updateSetting("username", 
                "varuna");
        userPreferences.updateSetting("password", 
                "secret");
     
        userSettings.clear();
        userSettings = userPreferences.getUserSettings();
        
        assertEquals("app settings update test", 
                "varuna", userSettings.get("username"));
        
        //Get all keys
        String [] prefKeys = 
                FileUtils.enumerationToStringArray(userSettings.keys());
        boolean found=false;
        for (int i=0; i<prefKeys.length; i++){
            if(prefKeys[i].indexOf("password") >=0 ){
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
        userPreferences.addSetting("newkey", "newvalue");
        
        //Remove a setting
        userPreferences.deleteSetting("newkey");
        
    }
}
