/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ustadmobile.app;

import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author varuna
 */
public class AppPref {
    
    //Default app settings.
    public static Hashtable appSettings;
    public static String appDataDir = null;
    static final String REC_STORE = "UstadMobileApp";
    
    //Constructor
    public AppPref() {
    }
    
    //set up default settings.
    private static void setDefaultPreferences() {
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
    
    public static void updateSetting(String key, String newValue){
        //Initiate app RMS..
        RMSUtils appRms = new RMSUtils(REC_STORE);
        
        //Get Current configuration
        Hashtable currentSettings = getAppSettings();
        if(currentSettings.containsKey(key)){
            currentSettings.remove(key);
            currentSettings.put(key, newValue);
            
            //Put it back in
            
            //Clear it, Close it
            appRms.deleteRMS();
            appRms.closeRMS();
            
            //Open it again
            appRms.openRMS();
            
            //Generate bytes
            byte[] newSettingsBytes = 
                    SerializedHashtable.hashTabletoStream(currentSettings);
            
            //Insert the data in.
            appRms.insertBytes(newSettingsBytes);
            
            
            
        }
        //close the app RMS
        appRms.closeRMS();
        
    }
    
    public static void addSetting(String key, String newValue){
        //Initiate app RMS..
        RMSUtils appRms = new RMSUtils(REC_STORE);
        
        //Get Current configuration
        Hashtable currentSettings = getAppSettings();
        if(currentSettings.containsKey(key)){
            currentSettings.remove(key);
            currentSettings.put(key, newValue);
            
            //Put it back in
            
            //Clear it, Close it
            appRms.deleteRMS();
            appRms.closeRMS();
            
            //Open it again
            appRms.openRMS();
            
            //Generate bytes
            byte[] newSettingsBytes = 
                    SerializedHashtable.hashTabletoStream(currentSettings);
            
            //Insert the data in.
            appRms.insertBytes(newSettingsBytes);
            
            
            
        }else{
            currentSettings.put(key, newValue);
            //Clear it, Close it
            appRms.deleteRMS();
            appRms.closeRMS();
            
            //Open it again
            appRms.openRMS();
            
            //Generate bytes
            byte[] newSettingsBytes = 
                    SerializedHashtable.hashTabletoStream(currentSettings);
            
            //Insert the data in.
            appRms.insertBytes(newSettingsBytes);
            
        }
        //close the app RMS
        appRms.closeRMS();
    }
    
    public static void deleteSetting(String key){
        //Initiate app RMS..
        RMSUtils appRms = new RMSUtils(REC_STORE);
        
        //Get Current configuration
        Hashtable currentSettings = getAppSettings();
        if(currentSettings.containsKey(key)){
            currentSettings.remove(key);
            
            //Clear it, Close it
            appRms.deleteRMS();
            appRms.closeRMS();
            
            //Open it again
            appRms.openRMS();
            
            //Generate bytes
            byte[] newSettingsBytes = 
                    SerializedHashtable.hashTabletoStream(currentSettings);
            
            //Insert the data in.
            appRms.insertBytes(newSettingsBytes);
      
        }
        //close the app RMS
        appRms.closeRMS();
    }
    
    public static Hashtable getAppSettings(){
        
        //getDefault values
        setDefaultPreferences();
        
        //Initiate app RMS..
        RMSUtils appRms = new RMSUtils(REC_STORE);
        
        //Check if there is anything..
        appRms.openRMS();
        byte[] appSettingsByteArrayRMS = appRms.readBytes();
        Hashtable appSettingsRMS = SerializedHashtable.streamToHashtable
                (appSettingsByteArrayRMS);
        //appRms.closeRMS();
        if (appSettingsRMS.isEmpty()){
            System.out.print("empty");
        }else{
            System.out.print("not empty");
        }
        System.out.print("Size is: " + appSettingsRMS.size());
        System.out.print("Size should be: " + appSettings.size());
        
        if (appSettingsRMS.isEmpty() || appSettingsRMS.size() < appSettings.size()){
            //wipe it.
            appRms.deleteRMS();
            
            //default hashtable to bytearray
            byte[] appSettingsByteArray = 
                SerializedHashtable.hashTabletoStream(appSettings);
            
            //load it with the default.
            appRms.insertBytes(appSettingsByteArray);
            
        }else{
            appSettings.clear();
            appSettings=appSettingsRMS;
            //appSettings.equals(appSettingsRMS);
        }
        
        //close the app RMS
        appRms.closeRMS();
        return appSettings;
    }
    
   
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

}
