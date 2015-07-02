/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ustadmobile.app;

import com.ustadmobile.impl.UstadMobileSystemImplJ2ME;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author varuna
 */
public class UserPref {
    
    //Default app settings. 
    public static Hashtable userSettings;
    public static String userDataDir = null;
    static final String REC_STORE = "UstadMobileUser";
    
    //Constructor
    public UserPref() {
    }
    
    //set up default settings.
    private static void setDefaultPreferences() {
        Hashtable defaultUserSettings = new Hashtable();
        defaultUserSettings.put("username", "");
        defaultUserSettings.put("password", "");
        defaultUserSettings.put("", "");
        //defaultUserSettings.put("", "");
        
        userSettings = new Hashtable();
        userSettings = defaultUserSettings;
    }
    
    public static void updateSetting(String key, String newValue){
        //Initiate app RMS..
        RMSUtils appRms = new RMSUtils(REC_STORE);
        
        //Get Current configuration
        Hashtable currentSettings = getUserSettings();
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
        Hashtable currentSettings = getUserSettings();
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
        Hashtable currentSettings = getUserSettings();
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
    
    public static String getSetting(String key){
        //Initiate app RMS..
        RMSUtils appRms = new RMSUtils(REC_STORE);
        String value = null;
        //Get Current configuration
        Hashtable currentSettings = getUserSettings();
        if(currentSettings.containsKey(key)){
            value = currentSettings.get(key).toString();
        }
        //close the app RMS
        appRms.closeRMS();
        return value;
    }
    
    public static Hashtable getUserSettings(){
        
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
        
        
        if (appSettingsRMS.isEmpty() || appSettingsRMS.size() < userSettings.size()){
            //wipe it.
            appRms.deleteRMS();
            
            //default hashtable to bytearray
            byte[] appSettingsByteArray = 
                SerializedHashtable.hashTabletoStream(userSettings);
            
            //load it with the default.
            appRms.insertBytes(appSettingsByteArray);
            
        }else{
            userSettings.clear();
            userSettings=appSettingsRMS;
            //appSettings.equals(appSettingsRMS);
        }
        
        //close the app RMS
        appRms.closeRMS();
        return userSettings;
    }
    
   
    /**
     * Find out where we should put the base folder by finding the root folder
     * with the maximum amount of space (this should be the memory card generally)
     */
    public static String getUserDataDir(){
        DeviceRoots bestRoot = FileUtils.getBestRoot();
        if (bestRoot==null){
            return null;
        }
        UstadMobileSystemImplJ2ME umsij = new UstadMobileSystemImplJ2ME();
        String userData = umsij.getSharedContentDir();
        String userFolder = FileUtils.joinPath(userData, 
                userSettings.get("username").toString());
        try{
            FileConnection bCon = (FileConnection)Connector.open(userFolder);
            if (!bCon.isDirectory()){
                bCon.mkdir();
            }
            bCon.close();
            userDataDir = userFolder;
            return userDataDir;
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
