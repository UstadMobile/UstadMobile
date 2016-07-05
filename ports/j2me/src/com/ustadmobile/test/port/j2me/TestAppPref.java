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
import com.ustadmobile.port.j2me.app.AppPref;
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
        String[] prefKeys = UMUtil.enumerationToStringArray(appSettings.keys());
        
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
