/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.controller;

import com.ustadmobile.app.DeviceRoots;
import com.ustadmobile.app.DeviceRoots;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.RMSUtils;
import com.ustadmobile.app.RMSUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
/**
 *
 * @author varuna
 */
public class UstadMobileAppController {
    
    /**
     * Dir to contain app data (e.g. logs, settings etc)
     */
    public static String appDataDir = null;
    
    public static final int MENUITEM_CATALOG = 0;
    public static final int MENUITEM_LIBRARY = 1;
    public static final int MENUITEM_DOWNLOAD = 2;
    public static final int MENUITEM_USERAUTH = 3;
    public static final int MENUITEM_ABOUT = 4;
    
    //Default app settings.
    public static Hashtable appSettings;
    private static void setupDefaultSettings() {
        Hashtable defaultAppSettings = new Hashtable();
        defaultAppSettings.put("umcloud", "http://umcloud1.ustadmobile.com");
        defaultAppSettings.put("tincan", 
                "http://umcloud1.ustadmobile.com/umlrs");
        defaultAppSettings.put("mboxsuffix", "@ustadmobile.com");
        defaultAppSettings.put("launched", 
                "http://adlnet.gov/expapi/verbs/launched");
        defaultAppSettings.put("opds", "http://umcloud1.ustadmobile.com/opds/");
        defaultAppSettings.put("opdspublic", 
                "http://umcloud1.ustadmobile.com/opds/public/");
        defaultAppSettings.put("lesson", 
                "http://adlnet.gov/expapi/activities/lesson");
        appSettings = new Hashtable();
        appSettings = defaultAppSettings;
    }
    
    public static void setDefaultAppSettings(){
        setupDefaultSettings();
    }
    
    public static Hashtable getAppSettings(){
        if (appSettings == null){
            setDefaultAppSettings();
        }
        return appSettings;
    }
    
    public static void getCurrentAppSettings(){
        
        //Get RMS config
        RMSUtils rms = new RMSUtils("UstadMobileApp");
        rms.openRMS();
        byte[] rmsByteArray = rms.readBytes();
        System.out.println("here..");
        Hashtable rmsHashtable = rms.readRMS();
        if(!rmsHashtable.contains("umcloud")){
            //HashTable aint existin..
            //RMS aint existin..
            //Hashtable defaultHashtable = 
            
        }
        rms.closeRMS();
        //return null;
    }
    
        //defaultMimeTypes.put("gif", "image/gif");
    
    /**
     * Find out where we should put the base folder by finding the root folder
     * with the maximum amount of space (this should be the memory card generally)
     */
    public static String getAppDataDir(){
        DeviceRoots bestRoot = FileUtils.getBestRoot();
        if (bestRoot==null){
            return null;
        }
        String baseFolder = bestRoot.path + "umobiledata";
        try{
            FileConnection bCon = (FileConnection)Connector.open(baseFolder);
            if (!bCon.isDirectory()){
                bCon.mkdir();
            }
            bCon.close();
            appDataDir = baseFolder;
            return appDataDir;
        }catch (Exception ce){
            return null;
        }
    }
    
    public static String getPlatform(){
        return System.getProperty("microedition.platform");
    }
    
    public static String getLocale(){
        return System.getProperty("microedition.locale");
    }

     /**
     * Find out where we should put the base folder by finding the root folder
     * with the maximum amount of space (this should be the memory card generally)
     */
    public static String setupAppDataDir() {
        String baseFolder = System.getProperty("fileconn.dir.photos") + 
                "umobiledata";
        
        try {
            FileConnection bCon = (FileConnection)Connector.open(baseFolder);
            if(!bCon.isDirectory()) {
                bCon.mkdir();
            }
            bCon.close();
            appDataDir = baseFolder;
            return baseFolder;//all OK
        }catch(Exception e3) {
        }
        return null;
    }
    
    public static XmlPullParser parseXml(InputStream is) throws 
            XmlPullParserException, IOException{
        KXmlParser parser = new KXmlParser();
        parser.setInput(is, "utf-8");
        return parser;
    }
    
    public final static String getBaseDir() {
        return appDataDir;
    }
    
    public static void handleMenuClick(int itemId){
        
        switch(itemId){
            case UstadMobileAppController.MENUITEM_LIBRARY:
                //go to page
                break;
            case UstadMobileAppController.MENUITEM_DOWNLOAD:
                //go to page
                break;
            case UstadMobileAppController.MENUITEM_USERAUTH:
                //go to page
                break;
            case UstadMobileAppController.MENUITEM_ABOUT:
                //go to page
                break;
            case UstadMobileAppController.MENUITEM_CATALOG:
                //go to page
                break;
        }
    }
   
}
