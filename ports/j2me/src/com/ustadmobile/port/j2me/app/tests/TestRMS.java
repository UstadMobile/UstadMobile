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
package com.ustadmobile.port.j2me.app.tests;

import com.ustadmobile.port.j2me.app.RMSUtils;
import com.ustadmobile.port.j2me.app.SerializedHashtable;
import com.ustadmobile.port.j2me.app.controller.UstadMobileAppController;
import j2meunit.framework.TestCase;
import java.util.Hashtable;

/**
 *
 * @author varuna
 */
public class TestRMS extends TestCase {
    
    public TestRMS(){
        setName("Testing RMS functionality.");
    }
    
    public void runTest() throws Throwable{
        
        UstadMobileAppController.getCurrentAppSettings();
        
        RMSUtils rms = new RMSUtils("UstadMobileApp");
        /*
        rms.deleteRMS();
        String string1 = "Hey, hows it going?";
        String string2 = "Oh not bad, hows the cat?";
        rms.openRMS();
        rms.writeRMS(string1);
        rms.writeRMS(string2);
        rms.closeRMS();
        rms.openRMS();
        Hashtable  rmsht;
        rmsht = rms.readRMS();
        rms.closeRMS();
        
        assertEquals("RMS read and write test success", string1,
                rmsht.get("1"));
        assertEquals("RMS read and write test success", string2,
                rmsht.get("2"));
        */
        Hashtable appSettings = UstadMobileAppController.getAppSettings();
        byte[] appSettingsByteArray = 
                SerializedHashtable.hashTabletoStream(appSettings);
        rms.deleteRMS();
        rms.openRMS();
        
        rms.insertBytes(appSettingsByteArray);
        rms.closeRMS();
        
        rms.openRMS();
        byte[] appSettingsByteArrayRMS = rms.readBytes();
        Hashtable appSettingsRMS = SerializedHashtable.streamToHashtable(
                appSettingsByteArrayRMS);
        
        assertEquals("RMS app Settings serialisable test", 
                appSettings.get("umcloud"), appSettingsRMS.get("umcloud"));
        
        rms.closeRMS();
        
        
    }
    
}
