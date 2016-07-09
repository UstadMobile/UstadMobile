/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.test.port.j2me;

import com.ustadmobile.core.util.UMUtil;
import j2meunit.framework.TestCase;
import java.util.Hashtable;
import com.ustadmobile.port.j2me.app.UserPref;

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
                "karmakid02");
        userPreferences.updateSetting("password", 
                "karmakid02");
     
        userSettings.clear();
        userSettings = userPreferences.getUserSettings();
        
        assertEquals("app settings update test", 
                "karmakid02", userSettings.get("karmakid02-username"));
        
        //Get all keys
        String [] prefKeys = 
                UMUtil.enumerationToStringArray(userSettings.keys());
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
