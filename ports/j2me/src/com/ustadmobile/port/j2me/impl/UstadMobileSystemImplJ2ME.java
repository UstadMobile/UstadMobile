/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.impl;

//import com.ustadmobile.app.controller.UstadMobileAppController;
import com.ustadmobile.port.j2me.app.AppPref;
import com.ustadmobile.port.j2me.app.DeviceRoots;
import com.ustadmobile.port.j2me.app.FileUtils;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import com.ustadmobile.core.impl.UMTransferJob;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.j2me.app.UserPref;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.view.AppView;
import java.io.InputStream;
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

    public UstadMobileSystemImplJ2ME() {
        
    }

    public boolean dirExists(String dirURI) throws IOException {
        return FileUtils.checkDir(dirURI);
    }

    public UMTransferJob downloadURLToFile(String url, String fileURI, 
            Hashtable headers) {
        return null;
        //HTTPUtils.downloadURLToFile(url, fileURI, "");
    }

    public UMTransferJob unzipFile(String zipSrc, String dstDir) {
        return null;
        //ZipUtils.unZipFile(zipSrc, dstDir);

    }

    public void setActiveUser(String username) {
        AppPref.addSetting("CURRENTUSER", username);
    }

    public void setUserPref(String key, String value) {
        UserPref.addSetting(key, value);
    }

    public String getUserPref(String key, String value) {
        return UserPref.getSetting(key);
    }

    public String[] getPrefKeyList() {
        return null;
    }

    public void saveUserPrefs() {
        
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
        return System.getProperty("microedition.locale").toString();
    }
    
    public Hashtable getSystemInfo(){
        Hashtable systemInfo = new Hashtable();
        try{
            systemInfo.put("platform", 
                    System.getProperty("microedition.platform").toString());
            systemInfo.put("encoding", 
                    System.getProperty("microedition.encoding").toString());
            systemInfo.put("configuration", 
                    System.getProperty("microedition.configuration").toString());
            systemInfo.put("profiles", 
                    System.getProperty("microedition.profiles").toString());
            systemInfo.put("locale", 
                    System.getProperty("microedition.locale").toString());
            systemInfo.put("memorytotal", 
                    Long.toString(Runtime.getRuntime().totalMemory()));
            systemInfo.put("memoryfree", 
                    Long.toString(Runtime.getRuntime().freeMemory()));
            return systemInfo;
        }catch (Exception e){}
        return null;
    }
    
    public String readFileAsText(String fileURI, String encoding) 
            throws IOException{
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
    
    public boolean removeFile(String fileURI) {
        boolean success = false;
        try {
            success = FileUtils.removeFileOrDir(fileURI, Connector.READ_WRITE,
            false);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (success == false){
            //Wanna do something?
        }
        return success;
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
        
    }

    public String getActiveUser() {
        //Code here:
        return AppPref.getSetting("CURRENTUSER");
    }

    public void setActiveUserAuth(String password) {
    }

    public String getActiveUserAuth() {
        return null;
    }

    public String getAppPref(String key) {       
        String value;
        value = AppPref.getSetting(key);
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
        String value = UserPref.getSetting(key);
        return value;
    }

    public AppView getAppView() {
        return null;
    }

    public UMLog getLogger() {
        return null;
    }

    public String openContainer(UstadJSOPDSEntry entry, String containerURI, 
            String mimeType) {
        return null;
    }

    public void closeContainer(String openURI) {
        //ToDo
    }

    public InputStream getFileInputStreamFromZip(String zipURI, String filename) {
        return null;
    }

}
