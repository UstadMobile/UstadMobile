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

package com.ustadmobile.core.impl;

import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.controller.UserSettingsController;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.util.MessagesHashtable;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.LoginView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import jp.sourceforge.qrcode.data.QRCodeImage;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
    import org.json.*;
/* $endif$ */


/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system (e.g. Android,
 * J2ME, etc)
 * 
 * 
 * @author mike
 */
public abstract class UstadMobileSystemImpl {
    
    protected static UstadMobileSystemImpl mainInstance;
    
    
    /**
     * Default behaviour - any existing content is overwritten
     */
    public static final int FILE_OVERWRITE = 1;
    
    /**
     * Flag to use with openFileOutputStream
     * 
     * @see UstadMobileSystemImpl#openFileOutputStream(java.lang.String, int) 
     */
    public static final int FILE_APPEND = 2;
    
    /**
     * Due to the nature of mobile app permission systems permission could be 
     * denied anytime IO takes place almost.  Therefor the IO methods of the
     * implementation should throw an IOException wrapper around any 
     * SecurityException that happens so it can be handled and presented to the
     * user as an error gracefully.
     */
    public static final String PREFIX_SECURITY_EXCEPTION = "SecurityException:";
    
    
    /**
     * Suggested name to create for content on Devices
     */
    public static final String CONTENT_DIR_NAME = "ustadMobileContent";
    
    private MessagesHashtable messages;
    
    /**
     * The direction - either 0 for LTR or 1 for RTL 
     */
    private int direction;
    
    private boolean initRan;
    
    /**
     * The currently active locale
     */
    private String locale;
    
    /**
     * The App Preference Key for the XAPIServer e.g. to get the active xAPI
     * server use getAppPref(UstadMobileSystemImpl.PREFKEY_XAPISERVER)
     */
    public static final String PREFKEY_XAPISERVER = "xapiserver";
    
    /**
     * Get an instance of the system implementation - relies on the platform
     * specific factory method
     * Indicates the number of bytes downloaded so far in a download
     */
    public static final int IDX_DOWNLOADED_SO_FAR = 0;

    /**
     * Indicates the total number of bytes in a download
     */
    public static final int IDX_BYTES_TOTAL = 1;

    /**
     * Indicates the status of a download (e.g. complete, failed, queued, etc)
     */
    public static final int IDX_STATUS = 2;
    
    
    
    /**
     * Flag to indicate a download requested was successful.
     * 
     * Same value as android.app.DownloadManager.STATUS_SUCCESSFUL
     */
    public static final int DLSTATUS_SUCCESSFUL = 8;
    
    /**
     * Flag to indicate a download requested has failed 
     * 
     * Same value as android.app.DownloadManager.STATUS_FAILED
     */
    public static final int DLSTATUS_FAILED = 16;
    
    /**
     * Flag to indicate download is pending
     * 
     * Same value as android.app.DownloadManager.STATUS_PENDING
     */
    public static final int DLSTATUS_PENDING = 1;
    
    /**
     * Flag to indicate download is running now
     * 
     * Same value as android.app.DownloadManager.STATUS_RUNNING
     */
    public static final int DLSTATUS_RUNNING = 2;
    
    /**
     * Flag to indicate download is paused (e.g. waiting to retry etc)
     * 
     * Same value as android.app.DownloadManager.STATUS_PENDING
     */
    public static final int DLSTATUS_PAUSED = 4;
    
    /**
     * Get an instance of the system implementation - relies on the platform
     * specific factory method
     * 
     * @return A singleton instance
     */
    public static UstadMobileSystemImpl getInstance() {
        if(mainInstance == null) {
            mainInstance = UstadMobileSystemImplFactory.createUstadSystemImpl();
        }
        
        return mainInstance;
    }
    
    /**
     * Convenience shortcut for logging
     * @see UMLog#l(int, int, java.lang.String) 
     * 
     * @param level log level
     * @param code log code
     * @param message message to log
     */
    public static void l(int level, int code, String message) {
        getInstance().getLogger().l(level, code, message);
    }
    
    /**
     * Convenience shortcut for logging
     * @see UMLog#l(int, int, java.lang.String, java.lang.Exception) 
     * 
     * @param level log level
     * @param code log code
     * @param message log message
     * @param exception exception that occurred to log
     */
    public static void l(int level, int code, String message, Exception exception) {
        getInstance().getLogger().l(level, code, message, exception);
    }
    
    /**
     * Do any required startup operations: init will be called on creation
     * 
     * This must make the shared content directory if it does not already exist
     */
    public void init(Object context) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 519, null);
        //We don't need to do init again
        if(initRan) {
            return;
        }
        
        try {
            checkCacheDir(context);
            loadActiveUserInfo(context);
            loadLocale(context);
            initRan = true;
        }catch(IOException e) {
            mainInstance.getLogger().l(UMLog.CRITICAL, 5, null, e);
        }
    }
    
    public void checkCacheDir(Object context) throws IOException{
        boolean sharedDirOK = false;
        String sharedContentDir = mainInstance.getSharedContentDir();
        sharedDirOK = mainInstance.makeDirectory(sharedContentDir);
        String sharedCacheDir = mainInstance.getCacheDir(
                CatalogController.SHARED_RESOURCE, context);
        boolean sharedCacheDirOK = mainInstance.makeDirectory(sharedCacheDir);
        StringBuffer initMsg = new StringBuffer(sharedContentDir).append(':').append(sharedDirOK);
        initMsg.append(" cache -").append(sharedCacheDir).append(':').append(sharedCacheDirOK);
        mainInstance.getLogger().l(UMLog.VERBOSE, 411, initMsg.toString());
    }
    
    /**
     * Go and start a new activity / view
     * 
     * @param cls What type of view to start
     * @param args 
     */
    public abstract void go(Class cls, Hashtable args, Object context);
    
    public boolean loadLocale(Object context) {
        //choose the locale
        boolean success = false;
        String usersLocale = null;
        if(getActiveUser(context) != null) {
            usersLocale = getUserPref(UserSettingsController.PREFKEY_LANG, "", 
                context);
        }
        
        locale = LocaleUtil.chooseSystemLocale(usersLocale,
                getSystemLocale(context), UstadMobileConstants.supportedLocales, 
                UstadMobileConstants.fallbackLocale);
        
        InputStream localeIn = null;
        try {
            localeIn = openResourceInputStream("locale/" +locale + ".properties",
                context);
            messages = MessagesHashtable.load(localeIn);
            getLogger().l(UMLog.VERBOSE, 423, locale);
            String localeDir = messages.get(U.id.dir);
            direction = localeDir != null && localeDir.equals("rtl") ? 
                UstadMobileConstants.DIR_RTL : UstadMobileConstants.DIR_LTR;
            success = true;
        }catch(IOException e) {
            getLogger().l(UMLog.CRITICAL, 9, null, e);
        }finally {
            UMIOUtils.closeInputStream(localeIn);
            localeIn = null;
        }
        
        return success;
    }
    
    /**
     * Provides the currently active locale
     * 
     * @return The currently active locale
     */
    public String getLocale() {
        return locale;
    }
    
    public abstract boolean loadActiveUserInfo(Object context);
    
    /**
     * Check on whether or not the locale string pack has been loaded or not
     * @return 
     */
    public boolean isLocaleLoaded() {
        return messages != null;
    }
    
    /**
     * Starts the user interface for the app
     */
    public void startUI(Object context) {        
        final UstadMobileSystemImpl impl = this;
        
        String activeUser = getActiveUser(context);
        String activeUserAuth = getActiveUserAuth(context);
        getLogger().l(UMLog.VERBOSE, 402, activeUser);
        
        
        if(activeUser == null || activeUserAuth == null) {
            go(LoginView.class, null, context);
        }else {
            Hashtable args = BasePointController.makeDefaultBasePointArgs(context);
            go(BasePointView.class, args, context);
            
        }
        
        /*
        if(activeUser == null || activeUserAuth == null) {
            go(LoginView.class, null, context);
        }else {
            Hashtable args = CatalogController.makeUserCatalogArgs(context);
            go(CatalogView.class, args, context);
        }*/

    }
    
    
    /**
     * Get a string for use in the UI
     * 
     * @param msgCode
     * @see U
     * @return String if found in current locale, otherwise null
     */
    public String getString(int msgCode) {
        return messages.get(msgCode);
    }
    
    /**
     * Gets the direction of the UI
     * 
     * @see UstadMobileConstants#DIR_LTR
     * @see UstadMobileConstants#DIR_RTL
     * 
     * @return Direction int flag - 0 for LTR or 1 for RTL
     */
    public int getDirection() {
        return direction;
    }
    
    /**
     * Get the name of the platform implementation being used
     * 
     * @return the name of the platform (used constructing views etc) e.g. "J2ME", "Android", etc
     */
    public abstract String getImplementationName();
    
    /**
     * Answer whether or not Javascript is supported (e.g. in WebViews) on this
     * platform 
     * 
     * @return true if supported (eg. Android) false otherwise (e.g. J2ME)
     */
    public abstract boolean isJavascriptSupported();
    
    /**
     * Queue the given TinCan statement represented in the JSON object 
     * for transmission to the tin can server
     * 
     * @param stmt statement to Queue
     * @return true if successfully queued, false otherwise
     */
    public abstract boolean queueTinCanStatement(JSONObject stmt, Object context);
    
    /**
     * Gets the cache directory for the platform for either user specific
     * cache contents / shared cache contents
     * 
     * @param mode USER_RESOURCE or SHARED_RESOURCE
     * @see CatalogController#USER_RESOURCE
     * @see CatalogController#SHARED_RESOURCE
     * @return String filepath to the cache dir for that mode
     */
    public abstract String getCacheDir(int mode, Object context);
    
    /**
     * Get storage directories
     * 
     * @param mode bitmask flag of USER_RESOURCE or SHARED_RESOURCE
     * @return Array of storage 
     */
    public abstract UMStorageDir[] getStorageDirs(int mode, Object context);
    
    /**
     * Provides the path to the shared content directory 
     * 
     * @deprecated - Use getStorageDirs and getCacheDirinstead
     * @return URI of the shared content directory
     */
    public abstract String getSharedContentDir();
    
    /**
     * Provides the path to content directory for a given user
     * 
     * @param username
     * @deprecated use getStorageDirs and getCacheDir instead
     * 
     * @return URI of the given users content directory
     */
    public abstract String getUserContentDirectory(String username);
    
    
    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     * 
     * @return System locale
     */
    public abstract String getSystemLocale(Object context);
    
    /**
     * Provide information about the platform as key value pairs in a hashtable
     * 
     * @return 
     */
    public abstract Hashtable getSystemInfo();
     
    /**
     * Read the given fileURI as a string and return it 
     * 
     * @param fileURI URI to the required file
     * @param encoding encoding e.g. UTF-8
     * 
     * @return File contents as a string
     */
    public  String readFileAsText(String fileURI, String encoding) throws IOException{
        getLogger().l(UMLog.DEBUG, 508, fileURI + " (" + encoding + ")");
        InputStream in = null;
        String result = null;
        IOException ioe = null;
        try {
            in = openFileInputStream(fileURI);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            UMIOUtils.readFully(in, bout, 1024);
            result = new String(bout.toByteArray());
        }catch(IOException e) {
            getLogger().l(UMLog.ERROR, 108, fileURI, e);
            ioe = e;
        }finally {
            UMIOUtils.closeInputStream(in);
        }
        
        UMIOUtils.throwIfNotNullIO(ioe);
        return result;
    }
    
    /**
     * Read the given fileURI as a string and return it - assume UTF-8 encoding
     * @param fileURI URI to the required file
     * @return Content of the file as a string with UTF-8 encoding
     */
    public String readFileAsText(String fileURI) throws IOException{
        return this.readFileAsText(fileURI, "UTF-8");
    }
    
    /**
     * Return the difference in file modification times between two files
     * 
     * @param fileURI1 
     * @param fileURI2
     * @return 
     */
    public abstract long modTimeDifference(String fileURI1, String fileURI2);
    
    /**
     * Get an output stream to the given file.  If the FILE_APPEND flag is set
     * then output will be appended to the end of the file, otherwise the file
     * will be overwritten if it exists already.
     * 
     * FILE_APPEND can be specified in the flags to append to the end of the file
     * 
     * @param fileURI URI to the file we want an output stream for
     * @param flags can set FILE_APPEND and FILE_AUTOCREATE
     */
    public abstract OutputStream openFileOutputStream(String fileURI, int flags) throws IOException, SecurityException;
    
    /**
     * Get an input stream from a given file
     * 
     * @param fileURI URI to the file for which we want an input stream
     */
    public abstract InputStream openFileInputStream(String fileURI) throws IOException, SecurityException;
    
    
    /**
     * Get an input stream for an item in the resources - this should be the path
     * without a leading slash for files that get copied from the res directory
     * of the source.
     * 
     * @param resURI the path to the resource; e.g. locale/en.properties
     */
    public abstract InputStream openResourceInputStream(String resURI, Object context) throws IOException;
    
    /**
     * Write the given string to the given file URI.  Create the file if it does 
     * not already exist.
     * 
     * @param str Content to write to the file
     * @param fileURI URI to the required file
     * @param encoding Encoding to use for string e.g. UTF-8
     */
    public void writeStringToFile(String str, String fileURI, String encoding) throws IOException {
        OutputStream out = null;
        IOException ioe = null;
        getLogger().l(UMLog.DEBUG, 500, fileURI + " enc " + encoding);
        try {
            out = openFileOutputStream(fileURI, FILE_OVERWRITE);
            out.write(str.getBytes(encoding));
            out.flush();
            getLogger().l(UMLog.DEBUG, 501, fileURI);
        }catch(IOException e) {
            getLogger().l(UMLog.ERROR, 106, fileURI + " enc:" + encoding, e);
            ioe = e;
        }finally {
            UMIOUtils.closeOutputStream(out);
            UMIOUtils.throwIfNotNullIO(ioe);
        }
    }
    
    /**
     * Check to see if the given file exists
     * 
     * @param fileURI URI of the file to check
     * @return true if exists and is a file or directory, false otherwise
     * 
     * @throws IOException 
     */
    public abstract boolean fileExists(String fileURI) throws IOException;
    
    /**
     * Check to see if the given URI exists and is a directory
     * 
     * @param dirURI URI to check if existing
     * @return true if exists and is a directory, false otherwise
     * @throws IOException 
     */
    public abstract boolean dirExists(String dirURI) throws IOException;
    
    /**
     * Remove the given file.  If the file does not exist, this method simply
     * returns false (also returns false if the file does exist but for some
     * other reason... e.g. permissions cannot be deleted).
     * 
     * @param fileURI URI to be removed
     * @return true if the file was successfully deleted, false otherwise
     */
    public abstract boolean removeFile(String fileURI);
    
    /**
     * List of files and directories within a directory as an array of Strings.
     * Should give only the relative path of the name within the directory
     * 
     * @param dirURI
     * @return
     * @throws IOException 
     */
    public abstract String[] listDirectory(String dirURI) throws IOException;
    
    public abstract long queueFileDownload(String url, String fileURI, Hashtable headers, Object context);
    
    public abstract int[] getFileDownloadStatus(long downloadID, Object context);
    
    public abstract void registerDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver, Object context);
    
    public abstract void unregisterDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver, Object context);
    
    /**
     * Rename file from/to 
     * 
     * @param fromFileURI current path / uri
     * @param toFileURI new path / uri
     * @return true if successful, false otherwise
     * 
     */
    public abstract boolean renameFile(String fromFileURI, String toFileURI);
    
    /**
     * Get the size of a file in bytes
     * 
     * @param fileURI File URI / Path
     * 
     * @return length in bytes
     */
    public abstract long fileSize(String fileURI);
    
    /**
     * Get the amount of free space available on a given file URI: This is of
     * course determined by the underlying filesystem
     * 
     * @param fileURI URI to the filesystem
     * 
     * @return The number of bytes available on the given filesystem : -1 for unknown
     */
    public abstract long fileAvailableSize(String fileURI) throws IOException;
    
    
    public abstract boolean makeDirectory(String dirURI) throws IOException;
    
    public abstract boolean removeRecursively(String dirURI);
    
    /**
     * Set the currently active user: the one that we need to know about for
     * preferences etc. currently
     * 
     * @param username username as a string, or null for no active user
     */
    public void setActiveUser(String username, Object context) {
        //Make sure there is a valid directory for this user
        getLogger().l(UMLog.INFO, 306, username);
        if(username != null) {
            String userCachePath = getCacheDir(CatalogController.USER_RESOURCE, 
                    context);
            String userCacheParent = UMFileUtil.getParentFilename(userCachePath);
            try {
                boolean dirOK = makeDirectory(userCacheParent) && makeDirectory(userCachePath);
                getLogger().l(UMLog.VERBOSE, 404, username + ":" + userCachePath 
                    + ":" + dirOK);
            }catch(IOException e) {
                getLogger().l(UMLog.CRITICAL, 3, username + ":" + userCachePath);
            }
        }
    }
    
    /**
     * Get the currently active user
     * 
     * @return Currently active username
     */
    public abstract String getActiveUser(Object context);
    
    /**
     * Set the authentication (e.g. password) of the currently active user
     * Used for communicating with server, download catalogs, etc.
     * 
     * @param password 
     */
    public abstract void setActiveUserAuth(String password, Object context);
    
    /**
     * Get the authentication (e.g. password) of the currently active user
     * 
     * @return The authentication (e.g. password) of the current user
     */
    public abstract String getActiveUserAuth(Object context);
    
    /**
     * Set a preference for the currently active user
     * 
     * @param key preference key as a string
     * @param value preference value as a string
     */
    public abstract void setUserPref(String key, String value, Object context);
    
    /**
     * Get a preference for the currently active user
     * 
     * @param key preference key as a string
     * @return value of that preference
     */
    public abstract String getUserPref(String key, Object context);
    
    
    /**
     * Get a preference for the currently active user 
     * 
     * @param key preference key as a string
     * @param defaultVal default value to return in case this is not set for this user
     * @return Value of preference for this user if set, otherwise defaultVal
     */
    public String getUserPref(String key, String defaultVal, Object context) {
        String valFound = getUserPref(key, context);
        return valFound != null ? valFound : defaultVal;
    }
    
    /**
     * Get a list of preference keys for currently active user
     * 
     * @return String array list of keys
     */
    public abstract String[] getUserPrefKeyList(Object context);
    
    /**
     * Trigger persisting the currently active user preferences.  This does NOT
     * need to be called each time when setting a preference, only when a user
     * logs out, program ends, etc.
     */
    public abstract void saveUserPrefs(Object context);
    
    /**
     * Get a preference for the app
     * 
     * @param key preference key as a string
     * @return value of that preference
     */
    public abstract String getAppPref(String key, Object context);
    
    /**
     * Get a list of preferences currently set for the app itself
     * 
     * @return String array list of app preference keys
     */
    public abstract String[] getAppPrefKeyList(Object context);
    
    /**
     * Get a preference for the app.  If not set, return the provided defaultVal
     *
     * @param key preference key as string
     * @param defaultVal default value to return if not set
     * @return value of the preference if set, defaultVal otherwise
     */
    public String getAppPref(String key, String defaultVal, Object context) {
        String valFound = getAppPref(key, context);
        return valFound != null ? valFound : defaultVal;
    }
    
    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     * 
     */
    public abstract void setAppPref(String key, String value, Object context);
    
    /**
     * Convenience method: setPref will use setUserPref if
     * isUser is true, setAppPref otherwise
     * 
     * @param isUserSpecific true if this is a user specific preference
     * @param key Preference key
     * @param value Value of preference to store
     */
    public void setPref(boolean isUserSpecific, String key, String value, Object context) {
        if(isUserSpecific) {
            setUserPref(key, value, context);
        }else {
            setAppPref(key, value, context);
        }
    }
    
    
    /**
     * Do a basic HTTP Request
     * 
     * @param url URL to request e.g. http://www.somewhere.com/some/thing.php?param1=blah
     * @param headers Hashtable of extra headers to add (can be null)
     * @param postParameters Parameters to be put in HTTP Request (can be null) 
     *  only applicable when method = POST
     * @param method e.g. GET, POST, PUT
     * @throws IOException if something goes wrong with the request
     * @return HTTPResult object containing the server response
     */
    public abstract HTTPResult makeRequest(String url, Hashtable headers, Hashtable postParameters, String method, byte[] postBody) throws IOException;
    
    
    /**
     * Do an HTTP request using the default method (GET)
     */
    public HTTPResult makeRequest(String url, Hashtable headers, Hashtable postParameters) throws IOException{
        return makeRequest(url, headers, postParameters, HTTPResult.GET, null);
    }

    /**
    * Do an HTTP request with no PostBody given
    */
    public HTTPResult makeRequest(String url, Hashtable headers, Hashtable postParameters, String method) throws IOException{
        return makeRequest(url, headers, postParameters, method, null);
    }
    
    /**
     * Reads a URL to String: this can be a file:/// url in which case the contents
     * will be read from the filesystem or an HTTP url
     * 
     * @param url file:/// url or http:// url
     * @param headers headers to send when an HTTP request (ignored in case of file:///)
     * 
     * @return HTTPResult with byte contents, status code if an HTTP request was made
     * @throws IOException 
     */
    public HTTPResult readURLToString(String url, Hashtable headers) throws IOException {
        l(UMLog.DEBUG, 521, url);
        String urlLower = url.toLowerCase();
        if(urlLower.startsWith("http://") || urlLower.startsWith("https://")) {
            return makeRequest(url, headers, null, "GET");
        }else if(urlLower.startsWith("file:///")) {
            String contents = readFileAsText(url);
            return new HTTPResult(contents.getBytes(), 200, null);
        }else {
            IOException e = new IOException("Unrecognized protocol: " + url);
            l(UMLog.ERROR, 127, url, e);
            throw e;
        }
    }
    
    /**
     * Make a new instance of an XmlPullParser (e.g. Kxml).  This is added as a
     * method in the implementation instead of using the factory API because
     * it enables the J2ME version to use the minimal jar 
     * 
     * @return A new default options XmlPullParser
     */
    public abstract XmlPullParser newPullParser() throws XmlPullParserException;
    
    /**
     * Make a new XmlPullParser from a given inputstream
     * @param in InputStream to read from
     * @param encoding Encoding to be used e.g. UTF-8
     * 
     * @return a new XmlPullParser with set with the given inputstream
     */
    public XmlPullParser newPullParser(InputStream in, String encoding) throws XmlPullParserException {
        l(UMLog.DEBUG, 523, encoding);
        XmlPullParser xpp = newPullParser();
        xpp.setInput(in, encoding);
        return xpp;
    }
    
    /**
     * Make a new XmlPullParser from a given inputstream assuming UTF-8 encoding
     * @param in InputStream to read from
     * @return a new XmlPullParser with set with the given inputstream
     * @throws XmlPullParserException 
     */
    public XmlPullParser newPullParser(InputStream in) throws XmlPullParserException {
        return newPullParser(in, UstadMobileConstants.UTF8);
    }
    
    /**
     * Generates a QRCodeImage compatible object for the platform that we're on
     * with the given input stream
     * 
     * @param in
     * 
     * @return 
     */
    public abstract QRCodeImage getQRCodeImage(InputStream in);
    
    /**
     * Get access to the App View to do common UI activities (e.g. show
     * progress dialog, flash message, etc)
     * 
     * @return Platform AppView
     */
    public abstract AppView getAppView(Object context);
    
    /**
     * Get access to the logger to use on this implementation
     * 
     * @return Platform logger
     */
    public abstract UMLog getLogger();
        
    /**
     * Open the given Zip file and return a ZipFileHandle for it.  This normally
     * means the underlying system will read through the entries in the zip
     * 
     * @param name Filename of the zip file
     * 
     * @return ZipFileHandle representing the zip opened
     */
    public abstract ZipFileHandle openZip(String name) throws IOException;
    
    /**
     * Figure out the screen size on this platform - can be used to determine
     * which file to download
     * 
     * @param context system dependent context object
     * 
     * @return int array with width, height in pixels
     * 
     * @throws IOException 
     */
    public abstract int[] getScreenSize(Object context);
    
}


