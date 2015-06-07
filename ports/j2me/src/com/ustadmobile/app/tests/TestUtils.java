/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import org.xmlpull.v1.XmlPullParser;
import com.ustadmobile.app.controller.UstadMobileAppController;
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
                "/com/ustadmobile/app/tests/test-settings.xml");
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
        String settingsDataURI = appDataURI +"/test-settings.xml";
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
