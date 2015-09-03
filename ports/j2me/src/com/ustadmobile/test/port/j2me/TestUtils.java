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

import com.ustadmobile.port.j2me.app.FileUtils;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import org.xmlpull.v1.XmlPullParser;
import com.ustadmobile.port.j2me.app.controller.UstadMobileAppController;
import java.io.IOException;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author varuna
 */
public class TestUtils {
    
    public static Hashtable testSettings;
    
    private static TestUtils mainInstance;
    
    public static TestUtils getInstance() {
        if(mainInstance == null) {
            mainInstance = new TestUtils();
        }
        
        return mainInstance;
    }
    
    public void loadTestSettingsResource() throws Exception { 
        InputStream is = getClass().getResourceAsStream(
                "/com/ustadmobile/test/port/j2me/test-settings.xml");
        XmlPullParser xpp = UstadMobileAppController.parseXml(is);
        testSettings = new Hashtable();
        String appDataURI = UstadMobileAppController.getAppDataDir();
        testSettings.put("appDataURI", appDataURI);
        int evtType = 0;
        //skip over root element tag
        xpp.nextTag();
        
        do {
            evtType = xpp.next();
            if(evtType == XmlPullParser.START_TAG) {
                String key = xpp.getName();
                String value = xpp.nextText();
                System.out.println(key +":"+value);
                testSettings.put(key, value);
            }
        }while(evtType != XmlPullParser.END_DOCUMENT);
        
        is.close();
    }
    
    public static void loadTestSettingsFile() throws Exception{
        //load from the file
        String appDataURI = UstadMobileAppController.getAppDataDir();
        String settingsDataURI = FileUtils.joinPath(appDataURI, "test-settings.xml");
        FileConnection fCon = (FileConnection)Connector.open(settingsDataURI,
            Connector.READ);
        InputStream is = fCon.openInputStream();
        XmlPullParser xpp = UstadMobileAppController.parseXml(is);
        testSettings = new Hashtable();
        testSettings.put("appDataURI", appDataURI);
        int evtType = 0;
        //skip over root element tag
        xpp.nextTag();
        
        do {
            evtType = xpp.next();
            if(evtType == XmlPullParser.START_TAG) {
                String key = xpp.getName();
                String value = xpp.nextText();
                testSettings.put(key, value);
            }
        }while(evtType != XmlPullParser.END_DOCUMENT);
        
        is.close();
        fCon.close();
        
    }
    
}
