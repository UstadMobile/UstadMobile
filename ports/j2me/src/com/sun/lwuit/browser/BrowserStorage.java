/*
 *  Copyright ? 2008, 2010, Oracle and/or its affiliates. All rights reserved
 */
package com.sun.lwuit.browser;

import com.sun.lwuit.io.Storage;
import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Handles saving of persistent data to the RMS.
 * This includes cookies, form data, history (visited links) and images/documents cache.
 *
 * The Hashtable format used for the cookies and form data is a Hashtable with a String key (representing cookie's domain or form action URL).
 * The value of the main Hastable is another Hashtable consisting of a String key (Cookie/Field name) and a String value (Cookie/Field value)
 *
 * For the history data the format is similar, but the value of the main hashtable is a vector holding strings of the visited links
 *
 * As for the cache, unlike all others the actual data is not saved in the memory 9due to its size).
 * An index containing all relevant links is kept in the memory in a simple vector (which is serialized to a separate record store).
 * The actual data is in a different record store where each record contains only the byte array representing the resource.
 * The record number of the index record store and the data record store match.
 *
 * NOTE: This class is not a scalabale implementation and can have some problems with memory or RMS sizes.
 * This is why the RMS can be disabled via the JAD properties - see RMS_ENABLED below.
 *
 * @author Ofir Leitner
 */
public class BrowserStorage  {

    /**
     * Cookies
     */
    public static final int TYPE_COOKIES = 0;

    /**
     * Form data
     */
    public static final int TYPE_FORM_DATA = 1;

    /**
     * History
     */
    public static final int TYPE_HISTORY = 2;

    /**
     * Cache
     */
    public static final int TYPE_CACHE = 3;

    static Vector cacheIndex;
    static String CACHE_RMS_INDEX_NAME = "cacheidx";
    static String CACHE_RMS_NAME = "cache";

    /**
     * The names of the record stores
     */
    static String[] RMS_NAMES = {"cookies","formdata","history",CACHE_RMS_NAME};

    /**
     * Inidcating which data types to store to the RMS.
     * This can be modified via the JAD property rms_* (where * is one of the RMS_NAMES)
     */
    static boolean[] RMS_ENABLED = {true,true,true,true};

    static Hashtable[] data = new Hashtable[3];


    /**
     * Returns a hashtable with all the cookies stored in the RMS
     * 
     * @return a hashtable with all the cookies stored in the RMS
     */
    public static Hashtable getCookies() {
        return getRMSData(TYPE_COOKIES);
    }

    /**
     * Returns a hashtable with all the form data stored in the RMS
     *
     * @return a hashtable with all the form data stored in the RMS
     */
    public static Hashtable getFormData() {
        return getRMSData(TYPE_FORM_DATA);
    }

    /**
     * Commits the cookies into the RMS. Should be called when the midlet terminates.
     */
    public static void commitCookies() {
        commitDataToRMS(TYPE_COOKIES);
    }

    /**
     * Commits the form data into the RMS. Should be called when the midlet terminates.
     */
    public static void commitFormData() {
        commitDataToRMS(TYPE_FORM_DATA);
    }

    /**
     * Adds the given cookie to be stored in the RMS (Actual saving is done on commitCookies)
     * 
     * @param domain The cookie's domain
     * @param name The cookie's name
     * @param value The cookie's value
     */
    public static void addCookie(String domain,String name,String value) {
        addDataRecord(TYPE_COOKIES, domain, name, value);
    }

    /**
     * Adds the given form data record to be stored in the RMS (Actual saving is done on commitFormData)
     * 
     * @param action The form's action URL
     * @param id The field's name
     * @param value The field's value
     */
    public static void addFormData(String action,String id,String value) {
        addDataRecord(TYPE_FORM_DATA, action, id, value);
    }

    /**
     * Clears all persistent cookies data.
     */
    public static void clearCookies() {
        clear(TYPE_COOKIES);
    }

    /**
     * Clears all persistent form data.
     */
    public static void clearFormData() {
        clear(TYPE_FORM_DATA);
    }

    /**
     * Clears all persistent history data, and returns the new and empty Hashtable
     * (Since unlike cookies and form data, the browser does not wokr on a copy, but on the actual Hashtable written to the RMS)
     */
    public static Hashtable clearHistory() {
        if (!RMS_ENABLED[TYPE_HISTORY]) {
            return new Hashtable();
        }
        data[TYPE_HISTORY]=new Hashtable();
        return data[TYPE_HISTORY];
    }

    /**
     * Clears the files (html/images) cache on the RMS
     */
    public static void clearCache() {
        Storage.getInstance().deleteStorageFile(CACHE_RMS_NAME);
        Storage.getInstance().deleteStorageFile(CACHE_RMS_INDEX_NAME);
        cacheIndex=null;
    }

    /**
     * Loads the index of the cache which includes all the saved resource names (Without their data)
     */
    private static void loadCacheIndex() {
        if (!RMS_ENABLED[TYPE_CACHE]) {
            return;
        }
        cacheIndex = (Vector)Storage.getInstance().readObject(CACHE_RMS_INDEX_NAME);
    }


    /**
     * Returns a resource (a stream to an image or HTML document) from the cache or null if none exists
     * 
     * @param url The URL to search for
     * @return a resource (a stream to an image or HTML document) from the cache or null if none exists
     */
    public static ByteArrayInputStream getResourcefromCache(String url) {
        if (!RMS_ENABLED[TYPE_CACHE]) {
            return null;
        }

        if (cacheIndex==null) {
            loadCacheIndex();
            if (cacheIndex==null) { //couldn't open indexes
                return null;
            }
        }
        int index=cacheIndex.indexOf(url);
        if (index==-1) { //Image not in cache
            //System.out.println("Resource not found in cache - "+url);
            return null;
        }
        return new ByteArrayInputStream((byte[])Storage.getInstance().readObject("img" + index));
    }

    /**
     * Adds the given resource (image/HTML) into the cache 
     * 
     * @param url The URL this resource should be linked to (For future cache search)
     * @param buf The resource buffer
     * @param updateIfExists when true this will override current cache value for this url, when false if it exists the new value will be ignored
     */
    public static void addResourceToCache(String url,byte[] buf,boolean updateIfExists) {
        if (!RMS_ENABLED[TYPE_CACHE]) {
            return;
        }

        if (cacheIndex==null) {
            loadCacheIndex();
            if (cacheIndex==null) { //couldn't open indexes
                return;
            }
        }
        int index=cacheIndex.indexOf(url);
        if ((!updateIfExists) && (index!=-1)) {
            //System.out.println("Image already exists - ignoring "+url);
            return;
        }
        if(index == -1) {
            index = 0;
        }

        Storage.getInstance().writeObject("img" + index, buf);
        cacheIndex.addElement(url);
        Storage.getInstance().writeObject(CACHE_RMS_INDEX_NAME, cacheIndex);
    }

   /**
    * Returns a Hashtable with the history data from the RMS.
    * The returned Hashtable is the same table that is stored in the Storage class, and thus all elements added to that table will be automatically
    * in the Storage hashtable and will be written on commitHistory
    *
    * @return A Hashtable with the history data
    */
    public static Hashtable getHistory() {
        if (!RMS_ENABLED[TYPE_HISTORY]) {
            return new Hashtable();
        }

       data[TYPE_HISTORY]=new Hashtable();
        Hashtable h = (Hashtable)Storage.getInstance().readObject(RMS_NAMES[TYPE_HISTORY]);
        if(h == null) {
            return new Hashtable();
        }
        return h;
   }

    public static void addHistory(String domain,String url) {
        if (!RMS_ENABLED[TYPE_HISTORY]) {
            return;
        }

        Vector urlData=(Vector)data[TYPE_HISTORY].get(domain);
        if (urlData==null) {
            urlData=new Vector();
            data[TYPE_HISTORY].put(domain,urlData);
        }
        urlData.addElement(url);
    }

    /**
     * Commits the history data to the RMS
     */
    public static void commitHistory() {
        if (!RMS_ENABLED[TYPE_HISTORY]) {
            return;
        }

        if (data[TYPE_HISTORY]==null) {
            //System.out.println("History RMS not opened");
            return;
        }
        Storage.getInstance().writeObject(RMS_NAMES[TYPE_HISTORY], data[TYPE_HISTORY]);
    }



   /**
    * Returns a Hashtable with the requested data from the RMS.
    * This both creates a hashtable used in this class, and creates another copy of the data that is returned.
    * The reason is that not all cookies should be saved (not all are persistent) so we have one copy for the session and another for saving.
    * 
    * @param type One of TYPE_COOKIES or TYPE_FORM_DATA
    * @return A Hashtable with the requested data
    */
    private static Hashtable getRMSData(int type) {
        if (!RMS_ENABLED[type]) {
            return new Hashtable();
        }
        Hashtable h = (Hashtable)Storage.getInstance().readObject(RMS_NAMES[type]);
        if(h == null) {
            return new Hashtable();
        }
        return h;
   }

   /**
    * Adds the given record to be saved in the RMS. Note that saving is not done immediately but only when commitDataToRMS is called
    * 
    * @param type One of TYPE_COOKIES or TYPE_FORM_DATA
    * @param url The URL, either the domain of the cookie or the action URL of the form
    * @param id The cookie name or field name
    * @param value The cookie value or field value
    */
    private static void addDataRecord(int type,String url,String id,String value) {
        if (!RMS_ENABLED[type]) {
            return;
        }
        Hashtable urlData=(Hashtable)data[type].get(url);
        if (urlData==null) {
            urlData=new Hashtable();
            data[type].put(url,urlData);
        }
        urlData.put(id,value);
    }

    /**
     * Commits the data (either cookies or form data) to the RMS. This should be called when the midlet termiantes.
     * 
    * @param type One of TYPE_COOKIES or TYPE_FORM_DATA
     */
    private static void commitDataToRMS(int type) {
        if (!RMS_ENABLED[type]) {
            return;
        }
        if (data[type]==null) {
            //System.out.println("RMS was not opened");
            return;
        }
        Storage.getInstance().writeObject(RMS_NAMES[type], data[type]);
    }

    /**
     * Clears the cookies/form data 
     * 
     * @param type One of TYPE_COOKIES or TYPE_FORM_DATA
     */
    private static void clear(int type) {
        if (!RMS_ENABLED[type]) {
            return;
        }
        data[type]=new Hashtable();
    }

}
