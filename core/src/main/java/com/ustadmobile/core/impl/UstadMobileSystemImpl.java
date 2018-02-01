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

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.opds.db.UmOpdsDbManager;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.util.MessagesHashtable;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.util.UMUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.LoginView;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.LinkedHashMap;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
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
    public static final String DEFAULT_CONTENT_DIR_NAME = "ustadmobileContent";

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
     * Schedule delay: The time since the logMananger initiates tincan queue
     * and the time between its occurrence
     */
    public static long SCHEDULE_DELAY= 2*60*1000;


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
     * Flag to indicate a download requested is retrying
     */
    public static final int DLSTATUS_RETRYING = 20;

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
     * Indicates that a download has not actually started yet
     */
    public static final int DLSTATUS_NOT_STARTED = 0;

    /**
     * The maximum number of sessions to show for the user to be able to resume
     * This is limited both for usability and performance.
     */
    public static final int RESUME_MAX_CHOICES = 5;

    /**
     * The return value from getLocale when the user has said to use the system's locale
     */
    public static final String LOCALE_USE_SYSTEM = "";



    protected static Hashtable MIME_TYPES = new Hashtable();

    protected static Hashtable MIME_TYPES_REVERSE = new Hashtable();

    static {
        MIME_TYPES.put("image/jpg", "jpg");
        MIME_TYPES.put("image/jpeg", "jpg");
        MIME_TYPES.put("image/png", "png");
        MIME_TYPES.put("image/gif", "gif");
        MIME_TYPES.put("image/svg", "svg");
        MIME_TYPES.put("application/epub+zip", "epub");

        MIME_TYPES_REVERSE = UMUtil.flipHashtable(MIME_TYPES);
    }

    /**
     * Get an instance of the system implementation - relies on the platform
     * specific factory method
     *
     * @return A singleton instance
     */
    public static UstadMobileSystemImpl getInstance() {
        if(mainInstance == null) {

            mainInstance = UstadMobileSystemImplFactory.makeSystemImpl();
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

        loadActiveUserInfo(context);

        initRan = true;
    }

    /**
     * Go to a new view : This is simply a convenience wrapper for go(viewName, args, context):
     * it will parse the a destination into the viewname and arguments, and then build a hashtable
     * to pass on.
     *
     * @param destination Destination name in the form of ViewName?arg1=val1&arg2=val2 etc.
     * @param context System context object
     */
    public void go(String destination, Object context) {
        Hashtable argsTable = null;
        int destinationQueryPos = destination.indexOf('?');
        if(destinationQueryPos == -1) {
            go(destination, null, context);
        }else {
            go(destination.substring(0, destinationQueryPos), UMFileUtil.parseURLQueryString(
                    destination), context);
        }
    }

    /**
     * The main method used to go to a new view. This is implemented at the platform level. On
     * Android this involves starting a new activity with the arguments being turned into an
     * Android bundle. On J2ME it creates a new Form and shows it, on iOS it looks up the related
     * UIViewController.
     *
     * @param viewName The name of the view to go to: This should match the view's interface .VIEW_NAME constant
     * @param args (Optional) Hahstable of arguments for the new view (e.g. catalog/container url etc)
     * @param context System context object
     */
    public abstract void go(String viewName, Hashtable args, Object context);

    /**
     * Provides the currently active locale
     *
     * @return The currently active locale code, or a blank "" string meaning the locale is the system default.
     */
    public String getLocale(Object context) {
        return locale;
    }

    /**
     * Provides the language code of the currently active locale. This is different to getLocale. If
     * the locale is currently set to LOCALE_USE_SYSTEM then that language will be resolved and the
     * code returned.
     *
     * @param context
     *
     * @return The locale as the user sees it.
     */
    public String getDisplayedLocale(Object context) {
        String locale = getLocale(context);
        if(locale.equals(LOCALE_USE_SYSTEM))
            locale = getSystemLocale(context);

        return locale;
    }

    public void setLocale(String locale, Object context) {
        this.locale = locale;
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

        if(CoreBuildConfig.LOGIN_BEFORE_FIRST_DESTINATION && (activeUser == null || activeUserAuth == null)) {
            go(LoginView.VIEW_NAME, null, context);
        }else {
            go(CoreBuildConfig.FIRST_DESTINATION, context);
        }

        /*
        if(activeUser == null || activeUserAuth == null) {

        }else {
            Hashtable args = BasePointController.makeDefaultBasePointArgs(context);
            go(BasePointView.VIEW_NAME, args, context);

        }
        */
    }

    /**
     * Save anything that should be written to disk
     */
    public synchronized void handleSave() {

    }

    /**
     * Get a string for use in the UI
     */
    public abstract String getString(int messageCode, Object context);


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
     * Answer whether or not this platform supports https
     *
     * @return True if the platform supports HTTPS , false otherwise (Temporarily - J2ME)
     */
    public abstract boolean isHttpsSupported();

    /**
     * Queue the given TinCan statement represented in the JSON object
     * for transmission to the tin can server
     *
     * @param stmt statement to Queue
     * @return true if successfully queued, false otherwise
     */
    public abstract boolean queueTinCanStatement(JSONObject stmt, Object context);


    /**
     * Add a managerTaskListener to be notified when the tincan queue is updated (items added/sent)
     * @param listener Listener to add
     */
    public abstract void addTinCanQueueStatusListener(TinCanQueueListener listener);

    /**
     * Remove a managerTaskListener from the list to be notified when tincan queue is updated
     *
     * @param listener
     */
    public abstract void removeTinCanQueueListener(TinCanQueueListener listener);


    /**
     * Gets the cache directory for the platform for either user specific
     * cache contents / shared cache contents
     *
     * @param mode USER_RESOURCE or SHARED_RESOURCE
     * @see CatalogPresenter#USER_RESOURCE
     * @see CatalogPresenter#SHARED_RESOURCE
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
    public abstract String getSharedContentDir(Object context);

    /**
     * Provides the path to content directory for a given user
     *
     * @param username
     * @deprecated use getStorageDirs and getCacheDir instead
     *
     * @return URI of the given users content directory
     */
    public abstract String getUserContentDirectory(Object context, String username);


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
     * Return the last file modification time of the given file
     *
     * @param fileURI
     *
     * @return The time the given file was last modified; or -1 in the event of an error
     */
    public abstract long fileLastModified(String fileURI);


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
     * Get an asset (from files that are in core/src/flavorName/assets)
     *
     */
    public abstract void getAsset(Object context, String path, UmCallback<InputStream> callback);


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

    public abstract int[] getFileDownloadStatus(String downloadID, Object context);

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
     * Make the given directory as per the dirURI parameter recursively creating
     * any new parent directories needed.  If the directory already exists this
     * method must simply return false and not throw an exception.
     *
     * @param dirURI Directory to be created
     *
     * @return true if successful, false otherwise
     * @throws IOException if an IOException occurs during the process
     */
    public abstract boolean makeDirectoryRecursive(String dirURI) throws IOException;

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
            String userCachePath = getCacheDir(CatalogPresenter.USER_RESOURCE,
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
     * Make an asynchronous http request. This can (on platforms with a filesystem) rely on the
     * caching directory.
     *
     * @param request request to make
     * @param responseListener response listener to receive response when ready
     */
    public abstract UmHttpCall makeRequestAsync(UmHttpRequest request,
                                                UmHttpResponseCallback responseListener);


    /**
     * Directly send an asynchronous http request. This must *NOT* rely on the httpcachedir, as it
     * will be used by HttpCacheDir as the underlying implementation to retrieve data from the network.
     *
     * @param request
     * @param responseListener
     * @return
     */
    public abstract UmHttpCall sendRequestAsync(UmHttpRequest request,
                                                   UmHttpResponseCallback responseListener);

    /**
     * Directly send a synchronous request. THIS IS NOT FOR NORMAL USAGE. It is intended only to be
     * used by the cache so requests can be pumped through the system http library, if present on
     * that implementation. As http libraries like okhttp
     *
     * It must *NOT* be used directly by presenters etc.
     *
     * @param request
     * @return
     */
    protected abstract UmHttpResponse sendRequestSync(UmHttpRequest request) throws IOException;


    public abstract UmHttpResponse makeRequestSync(UmHttpRequest request) throws IOException;


    /**
     * Mount a container (e.g. epub, xapi package, etc) so it can be accessed using makeRequest. This
     * normally means making the contents of a zip file accessible over http , e.g.
     * mount /path/file.zip will provide a base url, e.g. http://127.0.0.1:65000/file.zip and contents
     * can be accessed (e.g. http://127.0.0.1:65000/file.zip/some/file.xhtml )
     *
     * @param request The request to make
     * @param id The id used provided when the callback is called
     * @param callback Callback to call when the mount is completed or failed
     */
    public abstract void mountContainer(ContainerMountRequest request, int id, UmCallback callback);


    /**
     * Make a new instance of an XmlPullParser (e.g. Kxml).  This is added as a
     * method in the implementation instead of using the factory API because
     * it enables the J2ME version to use the minimal jar
     *
     * @return A new default options XmlPullParser
     */
    public abstract XmlPullParser newPullParser() throws XmlPullParserException;

    /**
     * Make a new instance of an XmlSerializer (org.xmlpull.v1.XmlSerializer)
     *
     * @return New instance of an XML Serializer
     */
    public abstract XmlSerializer newXMLSerializer();

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
     * When selecting a link to download we can use the mime type parameter
     * x-umprofile to determine the type of device the link is intended for
     * e.g. x-umprofile=micro for files with reduced size images and 3gp
     * video
     *
     * Currently supports only null (no specific profile) or micro
     *
     * @return profile name for this system e.g. null or "micro"
     */
    public abstract String getUMProfileName();

    /**
     * Return the mime type for the given extension
     *
     * @param extension the extension without the leading .
     *
     * @return The mime type if none; or null if it's not known
     */
    public String getMimeTypeFromExtension(String extension) {
        if(MIME_TYPES_REVERSE.containsKey(extension))
            return (String)MIME_TYPES_REVERSE.get(extension);

        return null;
    }

    /**
     * Return the extension of the given mime type
     *
     * @param mimeType The mime type
     *
     * @return File extension for the mime type without the leading .
     */
    public String getExtensionFromMimeType(String mimeType) {
        if(MIME_TYPES.containsKey(mimeType)) {
            return (String)MIME_TYPES.get(mimeType);
        }

        return null;
    }


    /**
     * Should a list of resumable registrations for the given activity id.  On
     * smartphone / desktop platforms this can be done talking to the local LRS.
     * On limited platforms this will need to be done differently.
     *
     * @param activityId The activity ID we are looking for registrations for
     */
    public abstract void getResumableRegistrations(String activityId, Object context, TinCanResultListener listener);

    /**
     * Gives a string with the version number
     *
     * @return String with version number
     */
    public abstract String getVersion(Object context);

    /**
     * Perform a one way hash of an authentication parameter
     *
     * @param context System context
     * @param auth Authentication secret to be hashed
     * @return The authentication secret hashed
     */
    public abstract String hashAuth(Object context, String auth);

    /**
     * Indicates whether or not this platform/device supports WiFi Direct (aka P2P WiFi)
     * @return
     */
    public boolean isWiFiP2PSupported() {
        return false;
    }


    public NetworkManagerCore getNetworkManager() {

        return null;
    }

    /**
     * Returns whether or not the init method has already been run
     *
     * @return true if init has been called with a first context used to load certain resources,
     * false otherwise
     */
    protected boolean isInitialized() {
        return initRan;
    }

    /**
     * Return absolute path of the application setup file. Asynchronous.
     *
     * @param context System context
     * @param zip if true, the app setup file should be delivered within a zip.
     * @param callback callback to call when complete or if any error occurs.
     * @return String: file absolute path
     */
    public abstract void getAppSetupFile(Object context, boolean zip, UmCallback callback);


    /**
     * Retrieve the progress for a given course entry id.
     *
     * @param entryIds
     * @return
     */
    public abstract CourseProgress getCourseProgress(String[] entryIds, Object context);

    /**
     * Register users locally
     * @param username
     * @param password
     * @param fields
     * @param context
     * @return
     */
    public abstract int registerUser(String username, String password, Hashtable fields, Object context);

    /**
     * Update users locally
     * @param username
     * @param password
     * @param fields
     * @param context
     * @return
     */
    public abstract int updateUser(String username, String password, Hashtable fields, Object context);

    /**
     * Authenticate locally
     * @param username
     * @param password
     * @return
     */
    public abstract boolean handleLoginLocally(String username, String password, Object context);

    /**
     * Create local user
     * @param username
     * @param password
     * @param uuid
     * @return
     */
    public abstract boolean createUserLocally(String username, String password, String uuid, Object context);

    /**
     * Provides a list of the content types which are supported on this platform.
     *
     * @return Array of Class objects representing the ContentTypePlugin
     */
    public abstract ContentTypePlugin[] getSupportedContentTypePlugins();

    /**
     * Format the given integer to use , seperators as per the locale in use
     *
     * @param integer
     *
     * @return
     */
    public abstract String formatInteger(int integer);

    /**
     * Wrapper to retrieve preference keys from the system Manifest.
     *
     * On Android: uses meta-data elements on the application element in AndroidManifest.xml
     * On J2ME: uses the jad file
     *
     * @param key The key to lookup
     * @param context System context object
     *
     * @return The value of the manifest preference key if found, null otherwise
     */
    public abstract String getManifestPreference(String key, Object context);

    /**
     * Wrapper to retrieve preference keys from the system Manifest.
     *
     * On Android: uses meta-data elements on the application element in AndroidManifest.xml
     * On J2ME: uses the jad file
     *
     * @param key The key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context System context object
     *
     * @return The value of the manifest preference key if found, otherwise the default value
     */
    public String getManifestPreference(String key, String defaultVal, Object context) {
        String val = getManifestPreference(key, context);
        if(val != null) {
            return val;
        }else {
            return defaultVal;
        }
    }


    /**
     * Lookup a value from the app runtime configuration. These come from a properties file loaded
     * from the assets folder, the path of which is set by the manifest preference
     * com.sutadmobile.core.appconfig .
     *
     * @param key The config key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context Systme context object
     *
     * @return The value of the key if found, if not, the default value provided
     */
    public abstract String getAppConfigString(String key, String defaultVal, Object context);

    /**
     * Get a boolean from the app configuration. App config is stored as a string, so this is
     * converted to a boolean using Boolean.parseBoolean
     *
     * @param key The preference key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context System context object
     * @return The boolean value of the given preference key if found, otherwise the default value
     */
    public boolean getAppConfigBoolean(String key, boolean defaultVal, Object context) {
        String strVal = getAppConfigString(key, null, context);
        if(strVal == null)
            return defaultVal;
        else
            return Boolean.parseBoolean(strVal);
    }

    /**
     * Get a boolean from the app configuration. App config is stored as a string, so this is
     * converted to a boolean using Boolean.parseBoolean
     *
     * @param key The preference key to lookup
     * @param context System context object
     * @return The boolean value of the given preference key if found, otherwise false
     */
    public boolean getAppConfigBoolean(String key, Object context) {
        return getAppConfigBoolean(key, false, context);
    }

    /**
     * Get an integer from the app configuration.
     *
     * @param key The preference key to lookup
     * @param defaultVal The default value if the preference key is not found
     * @param context System context object
     * @return The integer value of the value if found, otherwise the default value
     */
    public int getAppConfigInt(String key, int defaultVal, Object context) {
        return Integer.parseInt(getAppConfigString(key, ""+defaultVal, context));
    }

    public abstract String getUserDetail(String username, int field, Object dbContext);

    public abstract UmOpdsDbManager getOpdsDbManager();

    public abstract LinkedHashMap<String, String> getSyncHistory(Object node, Object context);

    public abstract LinkedHashMap<String, String> getMainNodeSyncHistory(Object context);

    public abstract long getMainNodeLastSyncDate(Object context);

    public abstract void triggerSync(Object context) throws Exception;

    public abstract String convertTimeToReadableTime(long time);

    /**
     * Determine if the two given locales are the same as far as what the user will see.
     *
     * @param oldLocale
     *
     * @return
     */
    public boolean hasDisplayedLocaleChanged(String oldLocale, Object context) {
        String currentlyDisplayedLocale = getDisplayedLocale(context);
        if(currentlyDisplayedLocale != null && oldLocale != null
                && oldLocale.substring(0, 2).equals(currentlyDisplayedLocale.substring(0,2))) {
            return false;
        }else {
            return true;
        }
    }

    protected final String getContentDirName(Object context) {
        return getAppConfigString(AppConfig.KEY_CONTENT_DIR_NAME, DEFAULT_CONTENT_DIR_NAME, context);
    }

}


