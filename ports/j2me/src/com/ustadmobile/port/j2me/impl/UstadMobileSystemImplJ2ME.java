/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.impl;

//import com.ustadmobile.app.controller.UstadMobileAppController;
import com.ustadmobile.app.AppPref;
import com.ustadmobile.app.DeviceRoots;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.HTTPUtils;
import com.ustadmobile.app.ZipUtils;
import com.ustadmobile.core.impl.UMTransferJob;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import javax.bluetooth.BluetoothConnectionException;
import com.ustadmobile.view.LoginViewJ2ME;
import com.ustadmobile.app.UserPref;
import com.ustadmobile.core.impl.HTTPResult;
import javax.microedition.io.file.FileConnection;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author varuna
 */
public class UstadMobileSystemImplJ2ME  extends UstadMobileSystemImpl {

    public String getImplementationName() {
        return "J2ME";
    }

    public boolean dirExists(String dirURI) throws IOException {
        return FileUtils.checkDir(dirURI);
    }

    public UMTransferJob downloadURLToFile(String url, String fileURI, Hashtable headers) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //HTTPUtils.downloadURLToFile(url, fileURI, "");
    }

    public UMTransferJob unzipFile(String zipSrc, String dstDir) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //ZipUtils.unZipFile(zipSrc, dstDir);

    }

    public void setActiveUser(String username) {
        AppPref.addSetting("CURRENTUSER", username);
    }

    public void setUserPref(String key, String value) {
        UserPref.addSetting(key, value);
        //throw new UnsupportedOperationException("Not supported yet."); 
        //To change body of generated methods, choose Tools | Templates.
    }

    public String getUserPref(String key, String value) {
        return UserPref.getSetting(key);
        //throw new UnsupportedOperationException("Not supported yet."); 
        //To change body of generated methods, choose Tools | Templates.
    }

    public String[] getPrefKeyList() {
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void saveUserPrefs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

    public UstadMobileSystemImplJ2ME() {
        
    }
    
    /**
     * Provides the path to the shared content directory 
     * 
     * @return URI of the shared content directory
     */
    public String getSharedContentDir(){ 
        //This will be in something like ustadmobileContent
        //appData is different
        try{
            DeviceRoots dt = FileUtils.getBestRoot();
            String sharedContentDir = FileUtils.joinPath(dt.path, 
                    FileUtils.USTAD_CONTENT_DIR);
            
            //Check if it is created. If it isnt, create it.       
            if(FileUtils.createFileOrDir(sharedContentDir, 
                    Connector.READ_WRITE, true)){
                return sharedContentDir;
            }
            
            //Return null if it doens't exist.
            if (!FileUtils.checkDir(sharedContentDir)){
                return null;
            }
        }catch (Exception e){}
        return null;
    }
    
    public String getUserContentDirectory(String username){
        try{
            DeviceRoots dt = FileUtils.getBestRoot();
            String sharedUserContentDir = dt.path + FileUtils.FILE_SEP + 
                    FileUtils.USTAD_CONTENT_DIR + FileUtils.FILE_SEP + username;
            
            //Return null if it doesn't exist
            if (!FileUtils.checkDir(sharedUserContentDir)){
                return null;
            }
            
            //Check if it is created. If it isn't, create it.
            if(FileUtils.createFileOrDir(sharedUserContentDir, 
                    Connector.READ_WRITE, true)){
                return sharedUserContentDir;
            }
            
        }catch (Exception e){}
        return null;
    }
    
    public String getSystemLocale(){
        //String device = UstadMobileAppController.getPlatform().toString();
        //String locale = UstadMobileAppController.getLocale().toString();
        return System.getProperty("microedition.locale").toString();
    }
    
    public Hashtable getSystemInfo(){
        Hashtable systemInfo = new Hashtable();
        try{
            systemInfo.put("platform", System.getProperty("microedition.platform").toString());
            systemInfo.put("encoding", System.getProperty("microedition.encoding").toString());
            systemInfo.put("configuration", System.getProperty("microedition.configuration").toString());
            systemInfo.put("profiles", System.getProperty("microedition.profiles").toString());
            systemInfo.put("locale", System.getProperty("microedition.locale").toString());
            systemInfo.put("memorytotal", Long.toString(Runtime.getRuntime().totalMemory()));
            systemInfo.put("memoryfree", Long.toString(Runtime.getRuntime().freeMemory()));
            //systemInfo.put("", System.getProperty("microedition.").toString());
            return systemInfo;
        }catch (Exception e){}
        return null;
    }
    
    public String readFileAsText(String fileURI, String encoding) throws IOException{
        try{
            String contents = FileUtils.getFileContents(fileURI);
            return contents;
        }catch(Exception e){}
        return null;
    }
    
    public String readFileAsText(String fileURI) throws IOException{
        return this.readFileAsText(fileURI, "UTF-8");
    }
    
    public long modTimeDifference(String fileURI1, String fileURI2){
        try{
            long file1LastModified = FileUtils.getLastModified(fileURI1);
            long file2LastModified = FileUtils.getLastModified(fileURI2);
            if (file1LastModified != -1 || file2LastModified != -1 ){
                long difference = file1LastModified - file2LastModified;
                return difference;
            }
        }catch(Exception e){}
        return -1;
    }
    
    public void writeStringToFile(String str, String fileURI, String encoding) 
            throws IOException{
        try{
            FileUtils.writeStringToFile(str, fileURI, true);
            
        }catch (Exception e){}
    }
    
    public boolean fileExists(String fileURI) throws IOException{
        return FileUtils.checkFile(fileURI);
    }
    
    public void removeFile(String fileURI) throws IOException{
        boolean success = FileUtils.removeFileOrDir(fileURI, Connector.READ_WRITE,
                false);
        if (success == false){
            //Wanna do something?
        }
    }

    public String[] listDirectory(String dirURI) throws IOException{
        
        String[] list = FileUtils.listFilesInDirectory(dirURI);
        
        return list;
    }
    
    public boolean renameFile(String fromFileURI, String toFileURI){
        boolean success = false;
        try {
            success = FileUtils.renameFileOrDir(fromFileURI, toFileURI, 
            Connector.READ_WRITE, false);
            if (success == false){
                //Wanna do something?
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return success;
       
    }
    
    public long fileSize(String fileURI){
        try{
            return FileUtils.getFileSize(fileURI);
        }catch(Exception e){}
        return -1;
    }
    
    public boolean makeDirectory(String dirURI) throws IOException{
 
        boolean createFileOrDir = FileUtils.createFileOrDir(dirURI, 
                Connector.READ_WRITE, true);
        if (!createFileOrDir){
            IOException e = new IOException();
            throw e;
        }
        return createFileOrDir;

    }
    
    public boolean removeRecursively(String dirURI){
        if (!dirURI.endsWith("/")){
            dirURI += "/";
        }
        try { 
            boolean success = FileUtils.deleteRecursively(dirURI, true);
            if (success){
                //Wanna do something?
            }else{
                IOException e = new IOException();
                throw e;
            }
            return success;
            //FileUtils.removeDirRecursively(dirURI, Connector.READ_WRITE);
            //FileUtils.removeFileOrDir(dirURI, Connector.READ_WRITE, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
        
    }
    
    public byte[] getHTTPResponseAsBytes(String url, Hashtable headers){
        
        return null;
    }

    public String getActiveUser() {
        //Code here:
        return AppPref.getSetting("CURRENTUSER");
    }

    public void setActiveUserAuth(String password) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getActiveUserAuth() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getAppPref(String key) {
        AppPref appPref = new AppPref();
        String value = null;
        value = appPref.getSetting(key);
        return value;
    }

    public HTTPResult makeRequest(String url, Hashtable headers, 
            Hashtable postParameters, String method) {
        try {
            return HTTPUtils.makeHTTPRequest(url, postParameters, headers, 
                    method);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void setAppPref(String key, String value) {
        AppPref.addSetting(key, value);
    }

    public XmlPullParser newPullParser() throws XmlPullParserException { 
        KXmlParser parser = new KXmlParser();
        return parser;
    }

    public String getUserPref(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
