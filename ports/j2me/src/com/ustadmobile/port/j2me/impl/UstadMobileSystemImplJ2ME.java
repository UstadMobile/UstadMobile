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
package com.ustadmobile.port.j2me.impl;

import com.sun.lwuit.Form;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.port.j2me.app.AppPref;
import com.ustadmobile.port.j2me.app.DeviceRoots;
import com.ustadmobile.port.j2me.app.FileUtils;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.j2me.app.UserPref;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.util.URLTextUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.port.j2me.impl.xapi.TinCanLogManagerJ2ME;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.UserSettingsView;
import com.ustadmobile.port.j2me.impl.zip.ZipFileHandleJ2ME;
import com.ustadmobile.port.j2me.util.J2MEIOUtils;
import com.ustadmobile.port.j2me.view.AppViewJ2ME;
import com.ustadmobile.port.j2me.view.CatalogViewJ2ME;
import com.ustadmobile.port.j2me.view.ContainerViewJ2ME;
import com.ustadmobile.port.j2me.view.LoginViewJ2ME;
import com.ustadmobile.port.j2me.view.UserSettingsViewJ2ME;
import com.ustadmobile.port.j2me.view.UstadViewFormJ2ME;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Timer;
import java.util.Vector;
import javax.microedition.io.Connection;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;
import org.json.me.JSONObject;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.json.me.*;
 
/**
 *
 * @author varuna
 */
public class UstadMobileSystemImplJ2ME  extends UstadMobileSystemImpl implements PlayerListener {

    private UMLog umLogger;
    
    private AppViewJ2ME appView;
    
    private Form currentForm;
    
    private ZipFileHandle openZip;
    
    private String openZipURI;
    
    public static final String OPENZIP_PROTO = "zip:///";
    
    private Player player;
    public int volumeLevel=70;
    
    private InputStream mediaInputStream;
    
    /**
     * System property used to get the memory card location
     */
    public static final String SYSTEMPROP_SDCARD = "fileconn.dir.memorycard";
    
    
    /**
     * System property used to get the photo dir location on device
     */
    public static final String SYSTEMPROP_PHOTODIR = "fileconn.dir.photos";
    
    /**
     * The shared content dir 
     */
    private String sharedContentDir = null;
    
    /**
     * The base directory where cache data etc is stored (e.g. opds catalogs etc)
     */
    private String baseSystemDir = null;
    
    private Timer logSendTimer;
    
    private TinCanLogManagerJ2ME logManager;
    
    private DownloadServiceJ2ME downloadService = null;
    
    private PlayerListener onEndOfMediaListener;
    
    public String getImplementationName() {
        return "J2ME";
    }

    public UstadMobileSystemImplJ2ME() {
        umLogger = new UMLogJ2ME();
        appView = new AppViewJ2ME(this);
        onEndOfMediaListener = null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isJavascriptSupported() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean queueTinCanStatement(JSONObject stmt, Object context) {
        l(UMLog.DEBUG, 538, "");
        return true;
        //return logManager.queueStatement(getActiveUser(context), stmt);
    }
    
    
    
    
    /**
     * {@inheritDoc}
     */
    public void init(Object context) {
        getCacheDir(CatalogController.SHARED_RESOURCE, context);
        try {
            makeDirectory(baseSystemDir);
            super.init(context);
        }catch(Exception e) {
            l(UMLog.CRITICAL, 7, baseSystemDir, e);
        }
        
        logSendTimer = new Timer();
        logManager = new TinCanLogManagerJ2ME();
        logSendTimer.scheduleAtFixedRate(logManager, (5*60*1000), (5*60*1000));
        downloadService = new DownloadServiceJ2ME();
        downloadService.load();
    }
    
    public static UstadMobileSystemImplJ2ME getInstanceJ2ME() {
        return (UstadMobileSystemImplJ2ME)mainInstance;
    }
    
    /**
     * This needs to be called so the system knows the current form
     * @param frm 
     */
    public void handleFormShow(Form frm) {
        l(UMLog.DEBUG, 525, frm.getTitle());
        if(this.currentForm != frm) {
            appView.dismissAll();
        }
        this.currentForm = frm;
    }
    
    public Form getCurrentForm() {
        return currentForm;
    }

    /**
     * {@inheritDoc} 
     */
    public boolean dirExists(String dirURI) throws IOException {
        l(UMLog.DEBUG, 527, dirURI);
        
        dirURI = dirURI.trim();
        if (!dirURI.endsWith("/")){
            dirURI += '/';
            l(UMLog.DEBUG, 529, dirURI);
        }
        
        boolean exists = false;
        FileConnection fc = null;
        IOException ioe = null;
        try {
            fc = (FileConnection) Connector.open(dirURI, 
                Connector.READ_WRITE);
            exists = fc.exists() && fc.isDirectory();
        }catch(IOException e) {
            ioe = e;
            l(UMLog.ERROR, 126, dirURI, e);
        }catch(SecurityException se) {
            ioe = new IOException(PREFIX_SECURITY_EXCEPTION +se.toString());
            l(UMLog.ERROR, 126, dirURI, se);
        }finally {
            J2MEIOUtils.closeConnection(fc);
        }
        
        UMIOUtils.throwIfNotNullIO(ioe);
        return exists;
    }

    
    public void setActiveUser(String username, Object context) {
        AppPref.addSetting("CURRENTUSER", username);
        UserPref.setActiveUser(username);
        
        if(username != null) {
            String userBaseDir = UMFileUtil.joinPaths(new String[] {baseSystemDir,
            username});
            try {
                makeDirectory(userBaseDir);
            }catch(IOException e) {
                l(UMLog.ERROR, 155, userBaseDir, e);
            }
        }
        
        super.setActiveUser(username, context);
        l(UMLog.DEBUG, 531, username);
    }

    public void setUserPref(String key, String value, Object context) {
        l(UMLog.DEBUG, 533, key + '=' + value);
        UserPref.addSetting(key, value);
    }

    public void saveUserPrefs(Object context) {
        
    }
    
    /**
     * Find a writable directory on the file system that is on the phone
     * (not removable) storage
     * 
     * @return Writable directory the phone's storage
     */
    public String findSystemBaseDir() {
        if(baseSystemDir == null) {
            l(UMLog.DEBUG, 589, null);
            Vector potentialCacheDirs = new Vector();
            
            String dirURI = System.getProperty(SYSTEMPROP_PHOTODIR);
            if(dirURI != null) {
                potentialCacheDirs.addElement(dirURI);
            }
            
            UMUtil.addEnumerationToVector(FileSystemRegistry.listRoots(), 
                    potentialCacheDirs);
            String currentDir;
            
            for(int i = 0; i < potentialCacheDirs.size(); i++) {
                currentDir = (String)potentialCacheDirs.elementAt(i);
                if(UMIOUtils.canWriteChildFile(currentDir)) {
                    baseSystemDir = UMFileUtil.joinPaths(new String[] {
                        currentDir, CONTENT_DIR_NAME});
                    break;
                }
            }
            l(UMLog.DEBUG, 589, baseSystemDir);
        }
        
        return baseSystemDir;
    }

    /**
     * {@inheritDoc}
     */
    public String getCacheDir(int mode, Object context) {
        findSystemBaseDir();
        
        if(mode == CatalogController.SHARED_RESOURCE) {
            return UMFileUtil.joinPaths(new String[] {baseSystemDir, 
                UstadMobileConstants.CACHEDIR});
        }else {
            return UMFileUtil.joinPaths(new String[]{baseSystemDir, 
                getActiveUser(context), UstadMobileConstants.CACHEDIR});
        }
    }

    /**
     * {@inheritDoc}
     */
    public UMStorageDir[] getStorageDirs(int mode, Object context) {
        //see if we can find the sdcard
        boolean incUserStorage = (mode & CatalogController.USER_RESOURCE) == CatalogController.USER_RESOURCE;
        boolean incSharedStorage = (mode & CatalogController.SHARED_RESOURCE) == CatalogController.SHARED_RESOURCE;
        
        Vector storageDirs = new Vector();
        String username = getActiveUser(context);
        
        if(incSharedStorage) {
            storageDirs.addElement(new UMStorageDir(baseSystemDir, 
                getString(U.id.phone_memory), false, true, false));
            l(UMLog.DEBUG, 591, storageDirs.elementAt(0).toString());
        }
        
        if(incUserStorage) {
            storageDirs.addElement(new UMStorageDir(UMFileUtil.joinPaths(
                new String[]{baseSystemDir, username}), getString(U.id.phone_memory), 
                false, true, true));
            l(UMLog.DEBUG, 591, storageDirs.elementAt(storageDirs.size()-1).toString());
        }
        
        boolean hasSDCard = false;
        boolean sdCardAvailable = false;
        
        String sdcardURI = System.getProperty(SYSTEMPROP_SDCARD);
        if(sdcardURI != null) {
            hasSDCard = true;
            try {
                sdCardAvailable = fileExists(sdcardURI) && 
                        UMIOUtils.canWriteChildFile(sdcardURI);
            }catch(IOException e) {
                l(UMLog.ERROR, 157, sdcardURI, e);
            }
        }
        
        if(hasSDCard) {
            String sdcardBaseDir = UMFileUtil.joinPaths(new String[]{sdcardURI, 
                CONTENT_DIR_NAME});
            if(incSharedStorage) {
                storageDirs.addElement(new UMStorageDir(sdcardBaseDir, 
                    getString(U.id.memory_card), true, sdCardAvailable, false));
                l(UMLog.DEBUG, 591, storageDirs.elementAt(storageDirs.size()-1).toString());
            }
            
            if(incUserStorage) {
                String userSDDir = UMFileUtil.joinPaths(
                    new String[] {sdcardBaseDir, username});
                storageDirs.addElement(new UMStorageDir(userSDDir, 
                    getString(U.id.memory_card), true, sdCardAvailable, true));
                l(UMLog.DEBUG, 591, storageDirs.elementAt(storageDirs.size()-1).toString());
            }
        }
        
        UMStorageDir[] retVal = new UMStorageDir[storageDirs.size()];
        storageDirs.copyInto(retVal);
        return retVal;
    }
    
    
    
    
    /**
     * {@inheritDoc} 
     */
    public String getSharedContentDir(){ 
        if(sharedContentDir != null) {
            return sharedContentDir;
        } else {
            Vector systemFsRoots = new Vector();
            String dirURI = System.getProperty(SYSTEMPROP_SDCARD);
            
            if(dirURI != null) {
                l(UMLog.DEBUG, 587, SYSTEMPROP_SDCARD + '=' + dirURI);
                systemFsRoots.addElement(dirURI);
            }

            dirURI = System.getProperty(SYSTEMPROP_PHOTODIR);
            if(dirURI != null) {
                l(UMLog.DEBUG, 587, SYSTEMPROP_PHOTODIR + '=' + dirURI);
                systemFsRoots.addElement(dirURI);
            }
            
            DeviceRoots dt = FileUtils.getBestRoot();
            if(dt != null && dt.path != null) {
                systemFsRoots.addElement(dt.path);
            }
            
            if(systemFsRoots.size() > 0) {
                boolean canUse = false;
                String currentDir = null;
                boolean exists = false;
                boolean isDir = false;
                boolean canWrite = false;
                
                for(int i = 0; i < systemFsRoots.size(); i++) {
                    FileConnection fc = null;
                    currentDir = (String)systemFsRoots.elementAt(i);
                    canUse = false;
                    canWrite = false;
                    
                    try {
                        fc = (FileConnection)Connector.open(currentDir);
                        exists = fc.exists();
                        String childFileUri = UMFileUtil.joinPaths(new String[] {
                            currentDir, "umfiletest.txt"});
                        if(exists) {
                            writeStringToFile("OK", childFileUri, "UTF-8");
                            canWrite = true;
                            canUse = true;
                        }
                    }catch(Exception e) {
                        l(UMLog.ERROR, 151, (String)systemFsRoots.elementAt(i), e);
                    }finally {
                        J2MEIOUtils.closeConnection(fc);
                    }
                    
                    if(canUse) {
                        l(UMLog.VERBOSE, 417, currentDir);
                        sharedContentDir = UMFileUtil.joinPaths(new String[]{ 
                            currentDir, CONTENT_DIR_NAME});
                        return sharedContentDir;
                    }else {
                        l(UMLog.VERBOSE, 419, currentDir + "exists:" + exists
                            + " dir:" + isDir + " write:" + canWrite);
                    }
                }
            } else {
                //This will be in something like ustadmobileContent
                //appData is different
                try{
                    dt = FileUtils.getBestRoot();
                    sharedContentDir = FileUtils.joinPath(dt.path, 
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
            }
        }
        
        l(UMLog.VERBOSE, 421, sharedContentDir);
        return null;
    }
    
    public String getUserContentDirectory(String username){
        String sharedDir = getSharedContentDir();
        return UMFileUtil.joinPaths(new String[] {sharedDir, username});
    }
    
    public String getSystemLocale(Object context){
        String locale = System.getProperty("microedition.locale");
        l(UMLog.DEBUG, 535, locale);
        return locale;
    }
    
    public Hashtable getSystemInfo(){
        Hashtable systemInfo = new Hashtable();
        
        systemInfo.put("platform", 
                System.getProperty("microedition.platform"));
        systemInfo.put("encoding", 
                System.getProperty("microedition.encoding"));
        systemInfo.put("configuration", 
                System.getProperty("microedition.configuration"));
        systemInfo.put("profiles", 
                System.getProperty("microedition.profiles"));
        systemInfo.put("locale", 
                System.getProperty("microedition.locale"));
        systemInfo.put("memorytotal", 
                Long.toString(Runtime.getRuntime().totalMemory()));
        systemInfo.put("memoryfree", 
                Long.toString(Runtime.getRuntime().freeMemory()));
        return systemInfo;
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
        
    /**
     * {@inheritDoc }
     */
    public boolean fileExists(String fileURI) throws IOException{
        l(UMLog.DEBUG, 537, fileURI);
        boolean fileExists = false;
        IOException e = null;
        try {
            FileConnection fc = (FileConnection)Connector.open(fileURI,
                Connector.READ);
            fileExists = fc.exists();
        }catch(IOException ioe) {
            e = ioe;
        }catch(SecurityException se) {
            e = new IOException(PREFIX_SECURITY_EXCEPTION  + se.toString());
        }
        
        UMIOUtils.logAndThrowIfNotNullIO(e, UMLog.ERROR, volumeLevel, fileURI);
        
        return fileExists;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean removeFile(String fileURI) {
        l(UMLog.DEBUG, 537, fileURI);
        boolean success = false;
        FileConnection con = null;
        
        try {
            con = (FileConnection)Connector.open(fileURI, Connector.READ_WRITE);
            if(con.exists()) {
                con.delete();
            }
            success = true;
        } catch (IOException ioe) {
            l(UMLog.ERROR, 129, fileURI, ioe);
        }catch(SecurityException se) {
            l(UMLog.ERROR, 129, fileURI, se);
        }finally {
            J2MEIOUtils.closeConnection(con);
        }
        
        return success;
    }

    /** 
     * {@inheritDoc}
     */
    public String[] listDirectory(String dirURI) throws IOException{
        l(UMLog.DEBUG, 539, dirURI);
        IOException e = null;
        
        if (!dirURI.endsWith("/")){
            dirURI += '/';
            l(UMLog.DEBUG, 541, dirURI);
        }
        
        FileConnection fc = null;
        String dirList[] = null;
        
        try {
            fc = (FileConnection) Connector.open(dirURI, 
                Connector.READ);
            Enumeration dirListEnu = fc.list();
            dirList = UMUtil.enumerationToStringArray(dirListEnu);
        }catch(IOException ioe) {
            e = ioe;
        }catch(SecurityException se) {
            e = new IOException(PREFIX_SECURITY_EXCEPTION+se.toString());
        }finally {
            J2MEIOUtils.closeConnection(fc);
        }
        
        UMIOUtils.logAndThrowIfNotNullIO(e, UMLog.ERROR, 131, dirURI);
        
        return dirList;
    }
    
    /**
     * 
     * @param fromFileURI
     * @param toFileURI
     * @return 
     */
    public boolean renameFile(String fromFileURI, String toFileURI){
        l(UMLog.DEBUG, 543, fromFileURI + "->" + toFileURI);
        boolean success = false;
        fromFileURI = fromFileURI.trim();
        toFileURI = toFileURI.trim();
        
        String fromParent = UMFileUtil.getParentFilename(fromFileURI);
        String toParent = UMFileUtil.getParentFilename(toFileURI);
        if((fromParent == null && toParent == null) || fromParent != null && fromParent.equals(toParent)) {
            //is the same parent directory - OK to move
            String newFilename = UMFileUtil.getFilename(toFileURI);
            FileConnection fc = null;
            try {
                fc = (FileConnection)Connector.open(fromFileURI, 
                        Connector.READ_WRITE);
                fc.rename(newFilename);
                success = true;
            }catch(IOException e) {
                l(UMLog.ERROR, 133, fromFileURI + "->" + toFileURI, e);
            }catch(SecurityException se) {
                l(UMLog.ERROR, 133, fromFileURI + "->" + toFileURI, se);
            }finally {
                J2MEIOUtils.closeConnection(fc);
            }
        }else {
            l(UMLog.ERROR, 135, fromFileURI + "->" + toFileURI);
        }
        
        return success;
       
    }
    
    public long fileSize(String fileURI){
        FileConnection fc = null;
        long size = -1;
        try {
            fc = (FileConnection) Connector.open(fileURI, Connector.READ);
            size = fc.fileSize();
        }catch(Exception e) {
            l(UMLog.ERROR, 137, fileURI, e);
        }finally {
            J2MEIOUtils.closeConnection(fc);
        }
        l(UMLog.VERBOSE, 545, fileURI + " (" + size + "bytes");
        return size;
    } 
    
    public boolean makeDirectory(String dirURI) throws IOException{
        getLogger().l(UMLog.VERBOSE, 401, dirURI);
        FileConnection fc = null;
        
        if(dirURI.charAt(dirURI.length()-1) != '/') {
            //dirURI = dirURI.substring(0, dirURI.length()-1);
            dirURI += '/'; 
            getLogger().l(UMLog.DEBUG, 504, dirURI);
        }
        
        
        IOException e = null;
        boolean dirOK = false;
        try {
            fc = (FileConnection)Connector.open(dirURI);
            getLogger().l(UMLog.DEBUG, 506, dirURI);
            if(!(fc.isDirectory() && fc.exists())) {
                fc.mkdir();
                dirOK = true;
                getLogger().l(UMLog.DEBUG, 503, dirURI);
            }else {
                getLogger().l(UMLog.DEBUG, 502, dirURI);
                dirOK = true;
            }
        }catch(IOException e2) {
            e = e2;
            getLogger().l(UMLog.ERROR, 104, dirURI, e);
        }finally {
            J2MEIOUtils.closeConnection(fc);
        }
        
        if(e != null) {
            throw e;
        }
        
        return dirOK;

    }
    
    public boolean removeRecursively(String dirURI){
        l(UMLog.DEBUG, 547, dirURI);
        
        if (!dirURI.endsWith("/")){
            dirURI += "/";
        }
        
        try {
            String[] fileNames = listDirectory(dirURI);
            String fullPath;
            for(int i = 0; i < fileNames.length; i++) {
                fullPath = UMFileUtil.joinPaths(new String[]{dirURI, fileNames[i]});
                if(fileNames[i].endsWith("/")) {
                    if(!removeRecursively(fullPath)) {
                        l(UMLog.ERROR, 139, fullPath);
                        return false;
                    }
                }else {
                    if(!removeFile(fullPath)) {
                        l(UMLog.ERROR, 141, fullPath);
                        return false;
                    }
                }
            }
            return removeFile(dirURI);
        }catch(Exception e) {
            l(UMLog.ERROR, 143, dirURI, e);
        }
        return false;
    }
    
    

    public String getActiveUser(Object context) {
        return AppPref.getSetting("CURRENTUSER");
    }

    /**
     * {@inheritDoc }
     */
    public void setActiveUserAuth(String password, Object context) {
        l(UMLog.DEBUG, 549, null);
        AppPref.addSetting("CURRENTUSERAUTH", password);
        //Adding password mapping for tincan log manager to pick up.
        String currentUsername = getActiveUser(context);
        AppPref.addSetting("password-"+currentUsername, password);
    }

    /**
     * {@inheritDoc }
     */
    public String getActiveUserAuth(Object context) {
        return AppPref.getSetting("CURRENTUSERAUTH");
    }

    /**
     * {@inheritDoc }
     */
    public String getAppPref(String key, Object ontext) {   
        String value;
        value = AppPref.getSetting(key);
        l(UMLog.DEBUG, 551, key + '=' + value);
        return value;
    }
 
    /**
     * @inheritDoc
     */
    public HTTPResult makeRequest(final String url, final Hashtable headers, final Hashtable postParameters, final String type, byte[] postBody) throws IOException{
        getLogger().l(UMLog.VERBOSE, 305, "HTTP (" + type + ")" + url);
        HTTPResult httpResult = null;
        HttpConnection httpConn = null;
        InputStream is = null;
        OutputStream os = null;
        IOException e = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        try {
            
            // Open an HTTP Connection object
            httpConn = (HttpConnection)Connector.open(url);
            httpConn.setRequestMethod(type);
            // Setup HTTP Request to GET/POST
            if(type.equals("POST")){
                httpConn.setRequestProperty("User-Agent",
                    "Profile/MIDP-1.0 Confirguration/CLDC-1.0");
                httpConn.setRequestProperty("Accept_Language","en-US");
                //Content-Type is must to pass parameters in POST Request
                httpConn.setRequestProperty("Content-Type", 
                        "application/x-www-form-urlencoded");
            }
            
            //Add Parameters
            String params = null;
            if (postParameters != null){
                Enumeration keys = postParameters.keys();
                String key, value;
                boolean firstAmp = true;
                while(keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    value = postParameters.get(key).toString();
                    value = URLTextUtil.urlEncodeUTF8(value);
                    if (firstAmp){
                        params = key + "=" + value;
                        firstAmp=false;
                    }else{
                        params = params + "&"+ key + "=" + value;
                    }
                }
            }

            l(UMLog.DEBUG, 800, "params are - " + params);
                        
            //Add Headers
            if (headers != null){
                Enumeration headerKeys = headers.keys();
                String hKey, hValue;
                while(headerKeys.hasMoreElements()) {
                    hKey = headerKeys.nextElement().toString();
                    hValue = headers.get(hKey).toString();
                    if(!hKey.equals("") && !hValue.equals("")){
                        l(UMLog.DEBUG, 800, "setting key " + hKey);
                        httpConn.setRequestProperty(hKey, hValue);
                    }
                }
            }
            
            if(type.equals("POST")){
                if(params == null && postBody != null){
                    //Content-Length to be set
                    l(UMLog.DEBUG, 800, "setting content length " + String.valueOf(postBody.length));
                    httpConn.setRequestProperty("Content-length", 
                            String.valueOf(postBody.length));
                    l(UMLog.DEBUG, 800, "setting property url to type " + type);
                    //httpConn.setRequestProperty(url, type);
                    l(UMLog.DEBUG, 800, "openingOutputStream");
                    os = httpConn.openOutputStream();
                    l(UMLog.DEBUG, 800, "writing params-getBytes()");
                    os.write(postBody);
                    l(UMLog.DEBUG, 800, "flushing..");
                    os.flush();
                    l(UMLog.DEBUG, 800, "flushed.");
                }else{
                    //Content-Length to be set
                    l(UMLog.DEBUG, 800, "setting content length " + String.valueOf(params.getBytes().length));
                    httpConn.setRequestProperty("Content-length", 
                            String.valueOf(params.getBytes().length));
                    l(UMLog.DEBUG, 800, "setting property url to type " + type);
                    //httpConn.setRequestProperty(url, type);
                    l(UMLog.DEBUG, 800, "openingOutputStream");
                    os = httpConn.openOutputStream();
                    l(UMLog.DEBUG, 800, "writing params-getBytes()");
                    os.write(params.getBytes());
                    //os.flush();
                }
                
            } 
            
            // Read Response from the Server
            int responseCode = httpConn.getResponseCode();
            is = httpConn.openInputStream();
            
            UMIOUtils.readFully(is, bout, 1024);
            
            byte[] response = null;
            response = bout.toByteArray();
            Hashtable responseHeaders = new Hashtable();
            String headerKey;
            String headerVal;
            int i = 0;
            while((headerKey = httpConn.getHeaderFieldKey(i++)) != null) {
                headerVal = httpConn.getHeaderField(headerKey);
                responseHeaders.put(headerKey, headerVal);
            }
            
            httpResult = new HTTPResult(response, responseCode, responseHeaders);
        }catch(IOException ioe){
            l(UMLog.ERROR, 124, url, ioe);
            e = ioe;
        }catch(SecurityException se) {
            e = new IOException(UstadMobileSystemImpl.PREFIX_SECURITY_EXCEPTION 
                + se.toString());
        }finally{
            UMIOUtils.closeInputStream(is);
            UMIOUtils.closeOutputStream(bout);
            J2MEIOUtils.closeConnection(httpConn);
        }
        
        UMIOUtils.throwIfNotNullIO(e);
        return httpResult;
    }

    public void setAppPref(String key, String value, Object context) {
        l(UMLog.DEBUG, 553, key + '=' + value);
        AppPref.addSetting(key, value);
    }

    public XmlPullParser newPullParser() throws XmlPullParserException { 
        KXmlParser parser = new KXmlParser();
        return parser;
    }

    public String getUserPref(String key, Object context) {
        String value = UserPref.getSetting(key);
        l(UMLog.DEBUG, 555, key + '=' + value);
        return value;
    }

    public AppView getAppView(Object context) {
        return appView;
    }

    public UMLog getLogger() {
        return umLogger;
    }

    /**
     * @inheritDoc
     */
    public String openContainer(String containerURI, String mimeType) {
        l(UMLog.DEBUG, 557, containerURI + " : " + mimeType);
        if(openZip != null) {
            throw new IllegalStateException("J2ME: Open one thing at a time please");
        }
        
        try {
            openZip = openZip(containerURI);
            openZipURI = containerURI;
        }catch(IOException e) {
            getLogger().l(UMLog.CRITICAL, 400, containerURI, e);
        }

        return OPENZIP_PROTO;
    }
    
    public ZipFileHandle getOpenZip() {
        return openZip;
    }

    public HTTPResult readURLToString(String url, Hashtable headers) throws IOException {
        if(url.startsWith(OPENZIP_PROTO)) {
            l(UMLog.DEBUG, 557, url);
            InputStream in = null;
            ByteArrayOutputStream bout = null;
            IOException ioe = null;
            try {
                in = openZip.openInputStream(url.substring(
                    OPENZIP_PROTO.length()));
                bout = new ByteArrayOutputStream();
                UMIOUtils.readFully(in, bout, 1024);
            }catch(IOException e) {
                getLogger().l(UMLog.INFO, 320, url, e);
                ioe = e;
            }finally {
                UMIOUtils.closeInputStream(in);
            }
            
            if(ioe == null) {
                return new HTTPResult(bout.toByteArray(), 200, new Hashtable());
            }else {
                throw ioe;
            }
        }else {
            return super.readURLToString(url, headers); 
        }
    }
    
    
    public void closeContainer(String openURI) {
        l(UMLog.DEBUG, 559, openURI);
        openZip = null;
    }

    public String[] getUserPrefKeyList(Object context) {
        return UserPref.getAllKeys();
    }

    public String[] getAppPrefKeyList(Object context) {
        return AppPref.getAllKeys();
    }

    public ZipFileHandle openZip(String name) throws IOException {
        l(UMLog.DEBUG, 559, name);
        return new ZipFileHandleJ2ME(name);
    }

    public OutputStream openFileOutputStream(String fileURI, int flags) throws IOException{
        l(UMLog.DEBUG, 599, fileURI);
        boolean append = (flags & FILE_APPEND) == FILE_APPEND;
        
        FileConnection con = null;
        IOException e = null;
        OutputStream out = null;
        
        try {
            con = (FileConnection)Connector.open(fileURI, Connector.READ_WRITE);
            
            if(con.exists()) {
                if(!append) {
                    con.delete();
                    con.create();
                }
            }else {
                con.create();
            }
        }catch(IOException e2) {
            e = e2;
        }catch(SecurityException se) {
            e = new IOException(PREFIX_SECURITY_EXCEPTION + se.toString());
        }finally {
            J2MEIOUtils.closeConnection(con);
            if(e != null) throw e;
        }

        if(!append) {
            out = Connector.openOutputStream(fileURI);
            
        }else {
            con = (FileConnection)Connector.open(fileURI, Connector.READ_WRITE);
            out = new ConnectorCloseOutputStream(
                con.openOutputStream(con.fileSize()), con);
        }
        
        return out;
        
    }

    /**
     * {@inheritDoc}
     */
    public InputStream openFileInputStream(String fileURI) throws IOException {
        l(UMLog.DEBUG, 599, fileURI);
        InputStream in = null;
        IOException e = null;
        try {
            in = Connector.openInputStream(fileURI);
        }catch(SecurityException se) {
            e = new IOException(PREFIX_SECURITY_EXCEPTION + se.toString());
        }
        UMIOUtils.throwIfNotNullIO(e);
        return in;
    }

    /**
     *{@inheritDoc}
     */
    public InputStream openResourceInputStream(String resURI, Object context) throws IOException {
        return getClass().getResourceAsStream("/res/" + resURI);
    }
    
    
    
    public String[] getPrefKeyList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public void go(Class cls, Hashtable args, Object context) {
        UstadViewFormJ2ME form = null;
        if(cls.equals(LoginView.class)) {
            form = new LoginViewJ2ME(args, context);
        }else if(cls.equals(CatalogView.class)) {
            form = new CatalogViewJ2ME(args, context);
        }else if(cls.equals(ContainerView.class)) {
            form = new ContainerViewJ2ME(args, context);
        }else if(cls.equals(UserSettingsView.class)) {
            form = new UserSettingsViewJ2ME(args, context);
        }
        
        form.show();
    }

    public boolean loadActiveUserInfo(Object context) {
        //nothing to do at the moment...
        return true;
    }

    public long queueFileDownload(String url, String fileURI, Hashtable headers, Object context) {
        return downloadService.enqueue(
            new DownloadServiceJ2ME.DownloadRequest(url, fileURI, headers, "GET"));
    }

    public int[] getFileDownloadStatus(long downloadID, Object context) {
        //TODO: implement this
        return downloadService.getStatus(downloadID);
    }

    public void registerDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver, Object context) {
        downloadService.registerDownloadCompleteReceiver(receiver);
    }

    public void unregisterDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver, Object context) {
        downloadService.unregisterDownloadCompleteReceiver(receiver);
    }
    
    /**
     * Plays the media's inputstream. Can be audio or video.
     * @param mediaInputStream the InputStream to be played.
     * @param encoding The encoding by which the player will get generated. 
     * 
     * @return true if play was started successfully, false otherwise
     */
    public boolean playMedia(InputStream mediaInputStream, String encoding) {
        return playMedia(mediaInputStream, encoding, null);
    }
    
    /**
     * Plays the media's inputstream. Can be audio or video.
     * @param mediaInputStream the InputStream to be played.
     * @param encoding The encoding by which the player will get generated. 
     * @param onEndOfMediaListener (Optional) a PlayerListener which will receive the END_OF_MEDIA event
     * 
     * @return true if play was started successfully, false otherwise
     */
    public boolean playMedia(InputStream mediaInputStream, String encoding, PlayerListener onEndOfMediaListener) {
        l(UMLog.DEBUG, 563, encoding);
        boolean status = false;
        stopMedia();
        
        this.onEndOfMediaListener = onEndOfMediaListener;
        this.mediaInputStream = mediaInputStream;
        
        VolumeControl vc = null;
        PlayerListener pl = null;
        
        
        try{
            player = Manager.createPlayer(mediaInputStream, encoding);
            player.addPlayerListener(this);
            player.realize();
            vc = (VolumeControl) player.getControl("VolumeControl");
            if (vc != null){
                vc.setLevel(volumeLevel);
            }
            player.start();
            long playerTime = player.getDuration();
            l(UMLog.DEBUG, 565, ""+playerTime);
            status = true;            
        }catch(Exception e){
            l(UMLog.ERROR, 145, encoding, e);
            if(onEndOfMediaListener != null) {
                onEndOfMediaListener.playerUpdate(player, PlayerListener.END_OF_MEDIA, null);
            }
            stopMedia();
        }
        
        return status;
    }

    /**
     * Handle playerUpdate events: specifically watch for END_OF_MEDIA and 
     * clean up when that is reached (inc. closing file input streams etc)
     * 
     * @param uPlayer
     * @param event
     * @param eventData 
     */
    public void playerUpdate(Player uPlayer, String event, Object eventData) {
        if (event.equals(PlayerListener.END_OF_MEDIA)) {
            PlayerListener endListener = onEndOfMediaListener;
            stopMedia();
            if(endListener != null) {
                endListener.playerUpdate(uPlayer, event, eventData);
            }
        }
    }
        
    /**
     * Stops the media playing. 
     * @return 
     */
    public boolean stopMedia() {
        l(UMLog.DEBUG, 567, null);
        boolean status = false;
        if (player != null){
            if(player.getState() != Player.CLOSED) {
                try {
                    player.stop();
                    player.deallocate();
                }catch(MediaException me) {
                    l(UMLog.ERROR, 177, null, me);
                }
            }
            
            player.close();
            player = null;
                        
            
            l(UMLog.DEBUG, 597, null);
        }else {
            l(UMLog.DEBUG, 569, null);
        }
        
        UMIOUtils.closeInputStream(mediaInputStream);
        status = true;
        mediaInputStream = null;
        onEndOfMediaListener = null;
        
        return status;
    }
    
    /**
     * Use when an output stream is bound to a connector, and we want to make sure
     * the connector gets closed when the outputstream is closed.
     * 
     * Simply calls Connection.close when the streams close method is called
     */
    public class ConnectorCloseOutputStream extends OutputStream {

        private OutputStream dst;
        
        private Connection con;
        
        public ConnectorCloseOutputStream(OutputStream dst, Connection con) {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 571, null);
            this.dst = dst;
        }
        
        public void write(int b) throws IOException {
            dst.write(b);
        }

        public void close() throws IOException {
            IOException ioe = null;
            try {
                dst.close();
            }catch(IOException e) {
                ioe = e;
            }finally {
                J2MEIOUtils.closeConnection(con);
            }
            UstadMobileSystemImpl.l(UMLog.DEBUG, 573, null);
            UMIOUtils.throwIfNotNullIO(ioe);
        }

        public void flush() throws IOException {
            dst.flush(); 
        }

        public void write(byte[] b, int off, int len) throws IOException {
            dst.write(b, off, len); 
        }

        public void write(byte[] b) throws IOException {
            dst.write(b); 
        }
    }

}
