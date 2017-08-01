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

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.UIManager;
import com.twmacinta.util.MD5;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.port.j2me.app.AppPref;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.j2me.app.UserPref;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.TinCanQueueListener;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.util.URLTextUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.port.j2me.impl.xapi.TinCanLogManagerJ2ME;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.UserSettingsView;
import com.ustadmobile.port.j2me.impl.zip.ZipFileHandleJ2ME;
import com.ustadmobile.port.j2me.util.J2MEIOUtils;
import com.ustadmobile.port.j2me.util.WatchedInputStream;
import com.ustadmobile.port.j2me.view.AppViewJ2ME;
import com.ustadmobile.port.j2me.view.BasePointViewJ2ME;
import com.ustadmobile.port.j2me.view.CatalogWrapperForm;
import com.ustadmobile.port.j2me.view.ContainerViewJ2ME;
import com.ustadmobile.port.j2me.view.LoginViewJ2ME;
import com.ustadmobile.port.j2me.view.UserSettingsViewJ2ME;
import com.ustadmobile.port.j2me.view.UstadViewFormJ2ME;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Timer;
import java.util.Vector;
import javax.microedition.io.Connection;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.midlet.MIDlet;
import org.json.me.JSONObject;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.json.me.*;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlSerializer;
 
/**
 *
 * @author varuna
 */
public class UstadMobileSystemImplJ2ME  extends UstadMobileSystemImpl {

    private UMLog umLogger;
    
    private AppViewJ2ME appView;
    
    private UstadViewFormJ2ME currentForm;
    
    private ZipFileHandle openZip;
    
    private String openZipURI;
    
    public static final String OPENZIP_PROTO = "zip:///";
    
    public static final String MICRO_PROFILE_NAME = "micro";
        
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
        
    private Vector viewHistory;
    
    public static final int VIEW_HISTORY_LIMIT = 10;
    
    private Hashtable mimeTypeToExtTable;
    
    private Hashtable extToMimeTypeTable;
    
    private MIDlet midlet;
    
    //#expand public static long BUILDSTAMP = %BUILDSTAMP%L;
    
    //#ifndef BUILDSTAMP
    public static long BUILDSTAMP = 0;
    //#endif
    
    public String getImplementationName() {
        return "J2ME";
    }

    public UstadMobileSystemImplJ2ME() {
        umLogger = new UMLogJ2ME();
        appView = new AppViewJ2ME(this);
        viewHistory = new Vector();
        
        //init the mime type list - built in hard coded
        mimeTypeToExtTable = new Hashtable();
        mimeTypeToExtTable.put("image/jpeg", "jpg");
        mimeTypeToExtTable.put("image/png", "png");
        mimeTypeToExtTable.put("image/gif", "gif");
        
        extToMimeTypeTable = new Hashtable();
        extToMimeTypeTable.put("jpg", "image/jpeg");
        extToMimeTypeTable.put("jpeg", "image/jpeg");
        extToMimeTypeTable.put("png", "image/png");
        extToMimeTypeTable.put("gif", "image/gif");
    }
    
    /**
     * Set the active midlet
     * 
     * @param midlet 
     */
    public void setMIDlet(MIDlet midlet) {
        this.midlet = midlet;
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
        l(UMLog.DEBUG, 538, null);

        if(logManager == null) {
            l(UMLog.DEBUG, 000, null);
            return false;
        }
        
        try {
            if(stmt.has("id")){
                stmt.put("id", UMTinCanUtil.generateUUID());
            }
        } catch (JSONException ex) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 639 , null, ex);
        }
        try {
            return logManager.queueStatement(getActiveUser(context), stmt);
        } catch (IOException ex) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 174, null, ex);
        }
        
        return false;
    }

    public void addTinCanQueueStatusListener(TinCanQueueListener listener) {
        l(UMLog.ERROR, 82, "addTCQL");
    }

    public void removeTinCanQueueListener(TinCanQueueListener listener) {
        l(UMLog.ERROR, 82, "removeTCQL");
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
        
        //logManager = new TinCanLogManagerJ2ME("");
        /*Check if the log manager timer task is supposed to 
        start. This might be set to false in some cases(eg: testing)
        */
        if (TinCanLogManagerJ2ME.AUTOSTART == true){
            logSendTimer = new Timer();
            /*Get the log directory. Generate if it doesn't exist*/
            String tincanDir = UMFileUtil.joinPaths(new String[]{
                findSystemBaseDir(), TinCanLogManagerJ2ME.LOG_FOLDER});
            try {
                makeDirectory(tincanDir);
            } catch (IOException ex) {
                l(UMLog.CRITICAL, 7, tincanDir, ex);
            }
           
            String tincanEndpointURL = UstadMobileDefaults.DEFAULT_XAPI_SERVER;
            logManager = new TinCanLogManagerJ2ME(tincanDir, tincanEndpointURL);
            /*SCHEDULE the log manager*/
            logSendTimer.scheduleAtFixedRate(logManager, SCHEDULE_DELAY,
                                                SCHEDULE_DELAY);
        }
        
        downloadService = new DownloadServiceJ2ME();
        downloadService.load();
        boolean isRTL = getDirection() == UstadMobileConstants.DIR_RTL;
        UIManager.getInstance().getLookAndFeel().setRTL(isRTL);
    }
    
    public static UstadMobileSystemImplJ2ME getInstanceJ2ME() {
        return (UstadMobileSystemImplJ2ME)mainInstance;
    }
    
    /**
     * This needs to be called so the system knows the current form
     * @param frm 
     */
    public void handleFormShow(UstadViewFormJ2ME frm) {
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
        
        if(username != null) {
            UserPref.setActiveUser(username);
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
                currentDir = UMFileUtil.ensurePathHasPrefix("file:///", currentDir);
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
                getString(MessageID.phone_memory, context), false, true, false));
            l(UMLog.DEBUG, 591, storageDirs.elementAt(0).toString());
        }
        
        if(incUserStorage) {
            storageDirs.addElement(new UMStorageDir(UMFileUtil.joinPaths(
                new String[]{baseSystemDir, username}), 
                getString(MessageID.phone_memory, context), 
                false, true, true));
            l(UMLog.DEBUG, 591, storageDirs.elementAt(storageDirs.size()-1).toString());
        }
        
        boolean hasSDCard = false;
        boolean sdCardAvailable = false;
        
        String sdcardURI = System.getProperty(SYSTEMPROP_SDCARD);
        l(UMLog.DEBUG, 592, sdcardURI);
        
        /* In case this device does not support the standard system property
         * to indicate the external memory card it might be listed in the roots.
         *
         * We will simply take the first root that is not used for the 
         */
        if(sdcardURI == null) {
            Enumeration rootList = FileSystemRegistry.listRoots();
            String cRoot;
            while(rootList.hasMoreElements() && sdcardURI == null) {
                cRoot = UMFileUtil.ensurePathHasPrefix("file:///", 
                    rootList.nextElement().toString());
                if(!baseSystemDir.startsWith(cRoot)) {
                    sdcardURI = cRoot;
                }
            }
        }
        
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
                    getString(MessageID.memory_card, context), true, sdCardAvailable, false));
                l(UMLog.DEBUG, 591, storageDirs.elementAt(storageDirs.size()-1).toString());
            }
            
            if(incUserStorage) {
                String userSDDir = UMFileUtil.joinPaths(
                    new String[] {sdcardBaseDir, username});
                storageDirs.addElement(new UMStorageDir(userSDDir, 
                    getString(MessageID.memory_card, context), true, sdCardAvailable, true));
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
        return findSystemBaseDir();
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
    
    public long fileLastModified(String fileURI) {
        long result = -1;
        FileConnection con = null;
        
        try {
            con = (FileConnection)Connector.open(fileURI);
            result = con.lastModified();
            l(UMLog.DEBUG, 570, fileURI + " :  " + result);
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 136, fileURI, e);
        }finally {
            J2MEIOUtils.closeConnection(con);
        }
        
        return result;
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
        
        UMIOUtils.logAndThrowIfNotNullIO(e, UMLog.ERROR, 600, fileURI);
        
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

    /**
     * {@inheritDoc}
     */
    public long fileAvailableSize(String fileURI) throws IOException {
        long result = -1;
        FileConnection fc = null;
        IOException ioe = null;
        try {
            fc = (FileConnection)Connector.open(fileURI);
            result = fc.availableSize();
            l(UMLog.DEBUG, 564, fileURI+ ":" + result);
        }catch(Exception e) {
            l(UMLog.ERROR, 137, fileURI, e);
            if(e instanceof IOException) {
                ioe = (IOException)e;
            }else {
                ioe = new IOException(e.toString() + ":" + e.getMessage());
            }
        }finally {
            J2MEIOUtils.closeConnection(fc);
        }
        
        UMIOUtils.throwIfNotNullIO(ioe);
        
        return result;
    }
    
    public boolean makeDirectoryRecursive(String dirURI) throws IOException{
        return makeDirectoryRecursive(dirURI, null);
    }
    
    public boolean makeDirectoryRecursive(String dirURI, Vector roots) throws IOException {
        l(UMLog.INFO, 372, dirURI);
        boolean created = false;
        try {
            if(roots == null) {
                roots = new Vector();
                UMUtil.addEnumerationToVector(FileSystemRegistry.listRoots(),
                    roots);
            }
            
            if(roots.contains(dirURI)) {
                created = true;
            }else if(dirURI.equals("file:///")) {
                created =  true;
            }else {
                if(makeDirectoryRecursive(UMFileUtil.getParentFilename(dirURI), roots)) {
                    return makeDirectory(dirURI);
                }
            }
        }catch(IOException e) {
            l(UMLog.ERROR, 186, dirURI, e);
            throw e;
        }
        
        return created;
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
                    "Profile/MIDP-2.0 Configuration/CLDC-1.0");
                httpConn.setRequestProperty("Accept_Language","en-US");
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
            
            /*
             * Important trivia on J2ME HTTP Post requests: J2ME will only use
             * Transfer-Encoding chunked which marks the length within the post
             * body itself - and therefor Content-Length headers must NOT be added.
             *
             * J2ME emulator will silently remove Content-Length headers but not
             * Content-length - even though HTTP headers are supposed to be case
             * insensitive
             */
            if(type.equals("POST")){
                byte[] toSend;
                if(params == null && postBody != null) {
                    toSend = postBody;
                }else {
                    toSend = params.getBytes(UstadMobileConstants.UTF8);
                    httpConn.setRequestProperty("Content-Type", 
                        "application/x-www-form-urlencoded");
                }
                
                os = httpConn.openOutputStream();
                os.write(toSend);
                os.flush();
                l(UMLog.DEBUG, 582, "" + toSend.length);
            } 
            
            // Read Response from the Server
            int responseCode = httpConn.getResponseCode();
            is = httpConn.openInputStream();
            
            UMIOUtils.readFully(is, bout, 1024);
            
            byte[] response = bout.toByteArray();
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

    public XmlSerializer newXMLSerializer() {
        return new KXmlSerializer();
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
                if(in != null) {
                    bout = new ByteArrayOutputStream();
                    UMIOUtils.readFully(in, bout, 1024);
                }else {
                    ioe = new IOException("Zip entry not found: " + url);
                }
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
    public InputStream openFileInputStream(String fileURI, String tag) throws IOException {
        l(UMLog.DEBUG, 599, fileURI);
        InputStream in = null;
        IOException e = null;
        try {
            in = new WatchedInputStream(Connector.openInputStream(fileURI), fileURI+ "!" + tag);
        }catch(SecurityException se) {
            e = new IOException(PREFIX_SECURITY_EXCEPTION + se.toString());
        }
        UMIOUtils.throwIfNotNullIO(e);
        return in;
    }
    
    public InputStream openFileInputStream(String fileURI) throws IOException{
        return openFileInputStream(fileURI, "");
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

    
    private UstadViewFormJ2ME getFormByArgs(String viewName, Hashtable args, Object context) {
        UstadViewFormJ2ME form = null;
        if(viewName.equals(LoginView.VIEW_NAME)) {
            form = new LoginViewJ2ME(args, context);
        }else if(viewName.equals(CatalogView.VIEW_NAME)) {
            form = new CatalogWrapperForm(args, context, true);
        }else if(viewName.equals(ContainerView.VIEW_NAME)) {
            form = new ContainerViewJ2ME(args, context);
        }else if(viewName.equals(UserSettingsView.VIEW_NAME)) {
            form = new UserSettingsViewJ2ME(args, context);
        }else if(viewName.equals(BasePointView.VIEW_NAME)) {
            form = new BasePointViewJ2ME(args, context, true);
        }
        
        return form;
    }

    private void destroyCurrentForm() {
        if(currentForm != null) {
            currentForm.onDestroy();
        }
    }
    
    public void go(String viewName, Hashtable args, Object context) {
        UstadViewFormJ2ME form = getFormByArgs(viewName, args, context);
        
        viewHistory.insertElementAt(new ViewHistoryEntry(viewName, args), 0);
        if(viewHistory.size() > VIEW_HISTORY_LIMIT) {
            viewHistory.setSize(VIEW_HISTORY_LIMIT);
        }
        
        
        destroyCurrentForm();
        currentForm = form;
        form.show();
    }
    
    public void goBack(Object context) {
        if(currentForm.canGoBack()) {
            currentForm.goBack();
        }else if(viewHistory.size() >= 2) {
            viewHistory.removeElementAt(0);
            ViewHistoryEntry entry = (ViewHistoryEntry)viewHistory.elementAt(0);
            UstadViewFormJ2ME frm = getFormByArgs(entry.viewName, entry.viewArgs, context);
            
            destroyCurrentForm();
            currentForm = frm;
            frm.showBack();
        }
    }
    
    /**
     * Returns the current number of history entries in the history list
     * 
     * @return 
     */
    public int getViewHistorySize() {
        return viewHistory.size();
    }

    public boolean loadActiveUserInfo(Object context) {
        //nothing to do at the moment...
        return true;
    }

    public String queueFileDownload(String url, String fileURI, Hashtable headers, Object context) {
        return downloadService.enqueue(
            new DownloadServiceJ2ME.DownloadRequest(url, fileURI, headers, "GET"));
    }

    public int[] getFileDownloadStatus(String downloadID, Object context) {
        return downloadService.getStatus(downloadID);
    }

    public void registerDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver, Object context) {
        downloadService.registerDownloadCompleteReceiver(receiver);
    }

    public void unregisterDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver, Object context) {
        downloadService.unregisterDownloadCompleteReceiver(receiver);
    }

    public boolean isHttpsSupported() {
        return false;
    }

    public String hashAuth(Object context, String auth) {
        try {
            MD5 md5 = new MD5();
            md5.Update(auth, null);
            return md5.asHex();
        }catch(UnsupportedEncodingException e) {
            l(UMLog.ERROR, 83, null, e);
            throw new RuntimeException(e.toString());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int[] getScreenSize(Object context) {
        return new int[] {Display.getInstance().getDisplayWidth(),
            Display.getInstance().getDisplayHeight()};
    }

    public String getUMProfileName() {
        return MICRO_PROFILE_NAME;
    }
    
    public String getMimeTypeFromExtension(String extension) {
        String lcExt = extension.toLowerCase();
        if(extToMimeTypeTable.containsKey(lcExt)) {
            return (String)extToMimeTypeTable.get(extension);
        }else {
            return null;
        }
    }

    public String getExtensionFromMimeType(String mimeType) {
        if(mimeTypeToExtTable.containsKey(mimeType)) {
            return (String)mimeTypeToExtTable.get(mimeType);
        }else {
            return null;
        }
    }

    /**
     * TODO: Implement resumable (attempt) registrations on J2ME
     * 
     * @param activityId
     * @param context
     * @param listener 
     */
    public void getResumableRegistrations(String activityId, Object context, TinCanResultListener listener) {
        //not implemented on J2ME yet
    }

    public long getBuildTime() {
        return BUILDSTAMP;
    }

    public String getVersion(Object context) {
        return midlet.getAppProperty("MIDlet-Version");
    }

    public String getString(int messageCode, Object context) {
        //TODO: Implement me
        return "";
    }

    public String getAppSetupFile(Object context) {
        //TODO: implement me
        return null;
    }

    public CourseProgress getCourseProgress(String[] entryIds, Object context) {
        //TODO: implement me
        return null;
    }

    public int registerUser(String username, String password, Hashtable fields, Object context) {
        //TODO implement me
        return -1;
    }

    public boolean handleLoginLocally(String username, String password, Object context) {
        //TODO: implement me
        return false;
    }

    public boolean createUserLocally(String username, String password, String uuid, Object context) {
        //TODO: implement me
        return false;
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
    
    public static class ViewHistoryEntry {
        
        String viewName;
        
        Hashtable viewArgs;
        
        public ViewHistoryEntry(String viewName, Hashtable viewArgs) {
            this.viewName = viewName;
            this.viewArgs = viewArgs;
        }
        
    }

}
