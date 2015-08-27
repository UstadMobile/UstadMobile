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

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.core.view.ViewFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
    
    public static UstadMobileSystemImpl getInstance() {
        if(mainInstance == null) {
            mainInstance = UstadMobileSystemImplFactory.createUstadSystemImpl();
            mainInstance.init();
        }
        
        return mainInstance;
    }
    
    /**
     * Do any required startup operations: init will be called on creation
     * 
     * This must make the shared content directory if it does not already exist
     */
    public void init() {
        
    }
    
    /**
     * Starts the user interface for the app
     */
    public void startUI() {
        //new LoginController().show();
        
        
        if(getActiveUser() == null) {
            new LoginController().show();
        }else {
            try {
                CatalogController.makeUserCatalog(this).show();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * Get the name of the platform implementation being used
     * 
     * @return the name of the platform (used constructing views etc) e.g. "J2ME", "Android", etc
     */
    public abstract String getImplementationName();
    
    
    /**
     * Provides the path to the shared content directory 
     * 
     * @return URI of the shared content directory
     */
    public abstract String getSharedContentDir();
    
    /**
     * Provides the path to content directory for a given user
     * 
     * @param username
     * 
     * @return URI of the given users content directory
     */
    public abstract String getUserContentDirectory(String username);
    
    
    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     * 
     * @return System locale
     */
    public abstract String getSystemLocale();
    
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
    public abstract String readFileAsText(String fileURI, String encoding) throws IOException;
    
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
     * Write the given string to the given file URI.  Create the file if it does 
     * not already exist.
     * 
     * @param str Content to write to the file
     * @param fileURI URI to the required file
     * @param encoding Encoding to use for string e.g. UTF-8
     */
    public abstract void writeStringToFile(String str, String fileURI, String encoding) throws IOException;
    
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
    
    /**
     * Downloads the contents of the given URL to the given file URI.  Overwrite
     * the fileURI if it already exists.
     * 
     * @param url HTTP URL to download from
     * @param fileURI file URI to save to
     * @param headers HTTP headers to set as key/value pairs
     * @return A transfer job that can be started and progress of which can be tracked
     */
    public abstract UMTransferJob downloadURLToFile(String url, String fileURI, Hashtable headers);
    
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
    
    public abstract boolean makeDirectory(String dirURI) throws IOException;
    
    public abstract boolean removeRecursively(String dirURI);
    
    /**
     * Gets an input stream for a specific file in a zip
     * 
     * @param zipURI e.g. /path/to/file.zip
     * @param filename File within the zip e.g. META-INF/container.xml
     * @return 
     */
    public abstract InputStream getFileInputStreamFromZip(String zipURI, String filename);
    
    /**
     * Set the currently active user: the one that we need to know about for
     * preferences etc. currently
     * 
     * @param username username as a string, or null for no active user
     */
    public void setActiveUser(String username) {
        //Make sure there is a valid directory for this user
        String userDirPath = getUserContentDirectory(username);
        try {
            makeDirectory(userDirPath);
        }catch(IOException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * Get the currently active user
     * 
     * @return Currently active username
     */
    public abstract String getActiveUser();
    
    /**
     * Set the authentication (e.g. password) of the currently active user
     * Used for communicating with server, download catalogs, etc.
     * 
     * @param password 
     */
    public abstract void setActiveUserAuth(String password);
    
    /**
     * Get the authentication (e.g. password) of the currently active user
     * 
     * @return The authentication (e.g. password) of the current user
     */
    public abstract String getActiveUserAuth();
    
    /**
     * Set a preference for the currently active user
     * 
     * @param key preference key as a string
     * @param value preference value as a string
     */
    public abstract void setUserPref(String key, String value);
    
    /**
     * Get a preference for the currently active user
     * 
     * @param key preference key as a string
     * @return value of that preference
     */
    public abstract String getUserPref(String key);
    
    
    /**
     * Get a preference for the currently active user 
     * 
     * @param key preference key as a string
     * @param defaultVal default value to return in case this is not set for this user
     * @return Value of preference for this user if set, otherwise defaultVal
     */
    public String getUserPref(String key, String defaultVal) {
        String valFound = getUserPref(key);
        return valFound != null ? valFound : defaultVal;
    }
    
    /**
     * Get a list of preference keys for currently active user
     * 
     * @return String array list of keys
     */
    public abstract String[] getPrefKeyList();
    
    /**
     * Trigger persisting the currently active user preferences.  This does NOT
     * need to be called each time when setting a preference, only when a user
     * logs out, program ends, etc.
     */
    public abstract void saveUserPrefs();
    
    /**
     * Get a preference for the app
     * 
     * @param key preference key as a string
     * @return value of that preference
     */
    public abstract String getAppPref(String key);
    
    /**
     * Get a preference for the app.  If not set, return the provided defaultVal
     *
     * @param key preference key as string
     * @param defaultVal default value to return if not set
     * @return value of the preference if set, defaultVal otherwise
     */
    public String getAppPref(String key, String defaultVal) {
        String valFound = getAppPref(key);
        return valFound != null ? valFound : defaultVal;
    }
    
    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     * 
     */
    public abstract void setAppPref(String key, String value);
    
    /**
     * Convenience method: setPref will use setUserPref if
     * isUser is true, setAppPref otherwise
     * 
     * @param isUserSpecific true if this is a user specific preference
     * @param key Preference key
     * @param value Value of preference to store
     */
    public void setPref(boolean isUserSpecific, String key, String value) {
        if(isUserSpecific) {
            setUserPref(key, value);
        }else {
            setAppPref(key, value);
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
     * @return HTTPResult object containing the server response
     */
    public abstract HTTPResult makeRequest(String url, 
            Hashtable headers, Hashtable postParameters, String method) throws IOException;
    
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
        String urlLower = url.toLowerCase();
        if(urlLower.startsWith("http://") || urlLower.startsWith("https://")) {
            return makeRequest(url, headers, null, "GET");
        }else if(urlLower.startsWith("file:///")) {
            String filePath = url.substring(7);
            String contents = readFileAsText(filePath);
            return new HTTPResult(contents.getBytes(), 200, null);
        }else {
            throw new IllegalArgumentException();
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
     * Get access to the App View to do common UI activities (e.g. show
     * progress dialog, flash message, etc)
     * 
     * @return Platform AppView
     */
    public abstract AppView getAppView();
    
    /**
     * Get access to the logger to use on this implementation
     * 
     * @return Platform logger
     */
    public abstract UMLog getLogger();
    
    /**
     * This method should open the given container and return a URI from
     * which it can be accessed.  Sometimes this may involve nothing more than
     * returning the input container path (e.g. in the case of a video that will
     * play locally from the file).  In the case of an EPUB or a zip container
     * this might involve it being mounted on a local http server (and then 
     * returning a local http URL for example).
     * 
     * This method should be assumed to take a while and be run in a SEPERATE
     * thread.
     * 
     * @param entry
     * @param containerPath
     * @param mimeType
     * @return 
     */
    public abstract String openContainer(UstadJSOPDSEntry entry, String containerURI, String mimeType);
    
    public abstract void closeContainer(String openURI);
}


