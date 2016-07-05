/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.util;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
    import org.json.*;
    import java.util.Iterator;
/* $endif$ */


/**
 *
 * Implements a basic HTTP cache dir to save things like image thumbnails etc.
 * to avoid wasting bandwidth and such that they can be used offline
 * 
 * @author mike
 */
public class HTTPCacheDir {
    
    private String dirName;
    
    private String indexFileURI;
    
    private JSONObject cacheIndex;
    
    private static final String INDEX_FILENAME = "cacheindex.json";
    
    public static final int IDX_EXPIRES = 0;
    
    public static final int IDX_ETAG = 1;
    
    public static final int IDX_LASTMODIFIED = 2;
    
    public static final int IDX_LASTACCESSED = 3;
    
    public static final int IDX_FILENAME = 4;
    
    public static final int IDX_MUSTREVALIDATE = 5;
    
    public static final int DEFAULT_EXPIRES = (1000 * 60 * 60);//1hour
    
    public static final String[] HTTP_MONTH_NAMES = new String[]{"Jan", "Feb",
        "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    
    public static final int[] HTTP_DAYS = new int[]{ Calendar.MONDAY, Calendar.TUESDAY,
        Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,
        Calendar.SUNDAY
    };
    
    public static final String[] HTTP_DAY_LABELS = new String[]{"Mon", "Tue",
        "Wed", "Thu", "Fri", "Sat", "Sun"};
    
    public static final int DEFAULT_MAX_ENTRIES = 200;
    
    private int maxEntries;
    
    /**
     * Starts a new HTTP cache directory in the directory given
     * 
     * @param dirName Directory to use for caching purposes
     * @param maxEntries the maximum number of entries to be allowed in the cache
     */
    public HTTPCacheDir(String dirName, int maxEntries) {
        this.dirName = dirName;
        this.maxEntries = maxEntries;
        
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        indexFileURI = UMFileUtil.joinPaths(new String[]{ dirName,
            INDEX_FILENAME});
        
        try {
            if(impl.fileExists(indexFileURI)) {
                cacheIndex = new JSONObject(impl.readFileAsText(indexFileURI, 
                    "UTF-8"));
            }
        }catch(Exception e) {
            impl.l(UMLog.ERROR, 134, indexFileURI, e);
        }
        
        if(cacheIndex == null) {
            cacheIndex = new JSONObject();
        }
    }
    
    public HTTPCacheDir(String dirName) {
        this(dirName, DEFAULT_MAX_ENTRIES);
    }
    
    
    
    
    private static int checkYear(int year) {
        if(year < 30) {
            return 2000 + year;
        }else if(year < 100) {
            return 1900 + year;
        }else {
            return year;
        }
    }
    
    /**
     * Appends two digits for the integer i; if i < 10; prepend a leading 0
     * 
     * @param i Numbe to append
     * @param sb StringBuffer to append it two
     * @return The stringbuffer
     */
    private static StringBuffer appendTwoDigits(int i, StringBuffer sb) {
        if(i < 10) {
            sb.append('0');
        }
        sb.append(i);
        
        return sb;
    }
    
    /**
     * Make a String for the date given by time as an HTTP Date as per 
     * http://tools.ietf.org/html/rfc2616#section-3.3 
     * 
     * e.g.
     * Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
     * 
     * @param time The time to generate the date for
     * @return A string with a properly formatted HTTP Date
     */
    public static String makeHTTPDate(long time) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(new Date(time));
        StringBuffer sb = new StringBuffer();
        
        int val = cal.get(Calendar.DAY_OF_WEEK);
        for(int i = 0; i < HTTP_MONTH_NAMES.length; i++) {
            if(val == HTTP_DAYS[i]) {
                sb.append(HTTP_DAY_LABELS[i]).append(", ");
                break;
            }
        }
        appendTwoDigits(cal.get(Calendar.DAY_OF_MONTH), sb).append(' ');
        
        sb.append(HTTP_MONTH_NAMES[cal.get(Calendar.MONTH)]).append(' ');
        sb.append(checkYear(cal.get(Calendar.YEAR))).append(' ');
        appendTwoDigits(cal.get(Calendar.HOUR_OF_DAY), sb).append(':');
        appendTwoDigits(cal.get(Calendar.MINUTE), sb).append(':');
        appendTwoDigits(cal.get(Calendar.SECOND), sb).append(" GMT");
        
        return sb.toString();
    }
    
    /**
     * Parse the given http date according to : 
     *  http://tools.ietf.org/html/rfc2616#section-3.3
     * 
     * @param httpDate
     * @return 
     */
    public static long parseHTTPDate(String httpDate) {
        char[] delimChars = new char[]{' ', ':', '-'};
        
        Vector tokens = UMUtil.tokenize(httpDate, delimChars, 0, httpDate.length());
        Calendar cal = null;
        
        if(tokens.size() == 8) {//this includes the timezone
            cal = Calendar.getInstance(TimeZone.getTimeZone(
                (String)tokens.elementAt(7)));
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(
                (String)tokens.elementAt(1)));
            cal.set(Calendar.MONTH, UMUtil.getIndexInArrayIgnoreCase(
                (String)tokens.elementAt(2), HTTP_MONTH_NAMES));
            cal.set(Calendar.YEAR, checkYear(Integer.parseInt(
                (String)tokens.elementAt(3))));
            
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(
                (String)tokens.elementAt(4)));
            cal.set(Calendar.MINUTE, Integer.parseInt(
                (String)tokens.elementAt(5)));
            cal.set(Calendar.SECOND, Integer.parseInt(
                (String)tokens.elementAt(6)));
        }else if(tokens.size() == 7) {
            cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(Calendar.MONTH, UMUtil.getIndexInArrayIgnoreCase(
                (String)tokens.elementAt(1), HTTP_MONTH_NAMES));
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(
                (String)tokens.elementAt(2)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(
                (String)tokens.elementAt(3)));
            cal.set(Calendar.MINUTE, Integer.parseInt(
                (String)tokens.elementAt(4)));
            cal.set(Calendar.SECOND, Integer.parseInt(
                (String)tokens.elementAt(5)));
            
            cal.set(Calendar.YEAR, checkYear(Integer.parseInt(
                (String)tokens.elementAt(6))));
        }else {
            throw new RuntimeException("Invalid date: " + httpDate);
        }
        
        return cal.getTime().getTime();
    }
    
    public boolean saveIndex() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.l(UMLog.DEBUG, 515, indexFileURI);
        boolean savedOK = false;
        
        try {
            impl.writeStringToFile(cacheIndex.toString(), indexFileURI, "UTF-8");
            savedOK = true;
        }catch(Exception e) {
            impl.l(UMLog.ERROR, 152, indexFileURI, e);
        }
        
        return savedOK;
    }
    
    
    private JSONArray getEntryByFilename(String filename) {
        boolean fileExists = false;
        try {
            fileExists = UstadMobileSystemImpl.getInstance().fileExists(
                UMFileUtil.joinPaths(new String[]{dirName, filename}));
        }catch(IOException e) {
            
        }
        
        if(!fileExists) {
            return null;
        }
        
        Enumeration e = getCachedURLs();
        String currentFilename;
        JSONArray currentEntry;
        String url;
        while(e.hasMoreElements()) {
            try {
                url = (String)e.nextElement();
                currentEntry = cacheIndex.getJSONArray(url);
                if(filename.equals(currentEntry.getString(IDX_FILENAME))) {
                    return currentEntry;
                }
            }catch(Exception ex) {
                //this should never happen - going through a JSON Object with known values
                ex.printStackTrace();
            }
        }
        
        return null;
    }
    
    /**
     * Run a get request through the cache.  This method will fetch the entry,
     * save it to the cache and return a file URI path to the entry itself.
     * 
     * @param url The HTTP or HTTPS url to fetch
     * @param filename Optional - can be null: A specific filename to use to 
     * save the entry in the cache.  When the same item might come from different
     * URLs this can be useful - e.g. OPDS catalogs by catalog id.
     * @param resultBuf Optional: An Array of 1 HTTPResult that in case we do wind
     * up directly fetching the result from the network this will be referenced
     * to the HTTPResult object.
     * @param fallback Optional: A fallback cache to send the get request to
     * in case we don't  find the entry in this cache.
     * 
     * @return The file URI path to the cached file
     * 
     * @throws IOException 
     */
    public String get(String url, String filename, Hashtable headers, HTTPResult[] resultBuf, HTTPCacheDir fallback) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        boolean isValid = false;
        String dataURL = null;
        
        if(url.startsWith("data:")) {
            dataURL = url;
            url = convertIfDataURL(url);
        }
        
        //check and see if we have the entry
        JSONArray entry = cacheIndex.optJSONArray(url);
        HTTPResult result = null;
        
        //if the filename is being used for an ID - lets see if that exists...
        if(entry == null && filename != null) {
            entry = getEntryByFilename(filename);
        }
        
        if (entry != null) {
            try {
                long timeNow = System.currentTimeMillis();
                long entryExpires = entry.getLong(IDX_EXPIRES);

                if (timeNow < entryExpires) {
                    //we can serve it direct from the cache - no validation needed
                    isValid = true;
                    impl.l(UMLog.DEBUG, 606, url);
                } else if(dataURL == null){
                    //needs to be validated
                    
                    result = impl.makeRequest(url, headers, null, "HEAD");
                    isValid = validateCacheEntry(entry, result);
                    impl.l(UMLog.DEBUG, 607, url + ':' + isValid);
                }
                
                if(isValid) {
                    entry.put(IDX_LASTACCESSED, timeNow);
                    return UMFileUtil.joinPaths(new String[]{ dirName, 
                        entry.optString(IDX_FILENAME)});
                }
            } catch (Exception e) {
                impl.l(UMLog.ERROR, 130, url, e);
            }

        }else if(fallback != null) {
            if(fallback.hasCachedURL(url) || (filename != null && fallback.hasCachedFilename(filename))) {
                return fallback.get(url, filename, headers, resultBuf, fallback);
            }
        }
        
        try {
            if(dataURL == null) {
                result = impl.makeRequest(url, headers, null);
            }else {
                result = new HTTPResult(dataURL);
            }
            
            if(resultBuf != null) {
                resultBuf[0] = result;
            }
            
            if(result.getStatus() >= 200 && result.getStatus() < 300) {
                return cacheResult(url, result, filename);
            }
        }catch(IOException e) {
            impl.l(UMLog.ERROR, 162, url, e);
        }
        
        //Looks like we are offline... use anything we have 
        if(entry != null) {
            return UMFileUtil.joinPaths(new String[]{ dirName, entry.optString(
                IDX_FILENAME)});
        }else {
            return null;
        }
    }
    
    private String convertIfDataURL(String url) {
        if(url.startsWith("data:")) {
            url = "datahc:///" + url.hashCode();
        }
        
        return url;
    }
    
    public String get(String url, String filename) throws IOException {
        return get(url, filename, null, null, null);
    }
    
    /**
     * 
     * @param url
     * @return
     * @throws IOException 
     */
    public String get(String url) throws IOException{
        return get(url, null, null, null, null);
    }
    
    /**
     * Gets the number of entries in the cache
     * 
     * @return Number of entries in the cache
     */
    public int getNumEntries() {
        return cacheIndex.length();
    }
    
    /**
     * Get the JSON array that represents the cached object
     * 
     * @param url The URL to lookup
     * 
     * @return JSONArray with values as per IDX_* constants
     */
    public JSONArray getEntry(String url) {
        try {
            return cacheIndex.optJSONArray(convertIfDataURL(url));
        }catch(Exception e) {
            //this really would never happen - using the opt method on a prebuilt jsonobject
        }
        return null;
    }
    
    public boolean validateCacheEntry(JSONArray entry, HTTPResult result) {
        boolean isValid = false;
        try {
            String validateHeader = result.getHeaderValue("etag");
            String cachedEtag = entry.optString(IDX_ETAG);
            if (validateHeader != null && validateHeader.equals(cachedEtag)) {
                isValid = true;
            }else if((validateHeader = result.getHeaderValue("Last-Modified")) != null){
                long cachedLastModified = entry.optLong(IDX_LASTMODIFIED, -1);
                if(cachedLastModified != -1 && parseHTTPDate(validateHeader) >= entry.getLong(IDX_LASTMODIFIED)) {
                    isValid = true;
                }
            }
        }catch(Exception e) {
            //JSON exception should really never happen here
            UstadMobileSystemImpl.l(UMLog.ERROR, 154, null, e);
        }
        
        return isValid;
    }
    
    /**
     * Used when the fileid that was used to save it is known (e.g. catalog
     * cache files which are saved by the catalog id)
     * 
     * @param filename
     * @return 
     */
    public String getCacheFileURIByFilename(String filename) {
        String fileURI = null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        boolean fileExists = false;
        try {
            fileURI = UMFileUtil.joinPaths(new String[]{dirName, filename});
            fileExists = impl.fileExists(fileURI);
        }catch(IOException e) {
            
        }
        
        return fileExists ? fileURI : null;
    }
    
    public String getCacheFileURIByURL(String url) {
        String fileURI = null;
        
        try {
            JSONArray urlEntry = cacheIndex.optJSONArray(convertIfDataURL(url));
            if(urlEntry != null) {
                urlEntry.put(IDX_LASTACCESSED, System.currentTimeMillis());
                fileURI = UMFileUtil.joinPaths(new String[] {dirName,
                    urlEntry.optString(IDX_FILENAME)});
            }
        }catch(Exception e) {
            //this should never happen - just digging a value out
            UstadMobileSystemImpl.l(UMLog.ERROR, 121, url, e);
        }
        
        return fileURI;
    }
    
    public boolean hasCachedURL(String url) {
        return cacheIndex.has(convertIfDataURL(url));
    }
    
    public boolean hasCachedFilename(String filename) {
        boolean exists = false;
        try {
            exists = UstadMobileSystemImpl.getInstance().fileExists(
                UMFileUtil.joinPaths(new String[]{dirName, filename}));
        }catch(IOException e) {
            
        }
        
        return exists;
    }
    
    
    /**
     * Calculates when an entry will expire based on it's HTTP headers: specifically
     * the expires header and cache-control header
     * 
     * As per :  http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html section
     * 14.9.3 the max-age if present will take precedence over the expires header
     * 
     * 
     * @param headers Hashtable with ALL headers in lower case
     * @return 
     */
    public static long calculateExpiryTime(Hashtable headers) {        
        if(headers.containsKey("cache-control")) {
            Hashtable ccParams = UMFileUtil.parseParams(
                (String)headers.get("cache-control"), ',');
            if(ccParams.containsKey("max-age")) {
                long maxage = Integer.parseInt((String)ccParams.get("max-age"));
                return System.currentTimeMillis() + (maxage * 1000);
            }
        }
        
        if(headers.containsKey("expires")) {
            return parseHTTPDate((String)headers.get("expires"));
        }
        
        return -1;
    }
    
    /**
     * Remove the given entry from the cache
     * 
     * @param url The URL of the item to remove from the cache
     * @return 
     */
    public boolean deleteEntry(String url) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        boolean result = false;
        try {
            JSONArray item  = cacheIndex.optJSONArray(url);
            if(item != null) {
                String fileURI = UMFileUtil.joinPaths(new String[] {dirName,
                    item.getString(IDX_FILENAME)});
                result = impl.removeFile(fileURI);
                if(result) {
                    cacheIndex.remove(url);
                }
            }
        }catch(Exception e) {
            impl.l(UMLog.ERROR, 156, url, e);
        }
        
        return result;
    }
    
    /**
     * Make an enumeration of all the urls that are in the cache
     * 
     * @return 
     */
    public Enumeration getCachedURLs() {
        Enumeration urls;
        /* $if umplatform == 2  $
            urls = cacheIndex.keys();
         $else$ */
            final Iterator urlIterator = cacheIndex.keys();
            urls = new Enumeration() {
                public boolean hasMoreElements() {
                    return urlIterator.hasNext();
                }

                public Object nextElement() {
                    return urlIterator.next();
                }
                
            };
        /* $endif$ */
        
        return urls;
    }
    
    /**
     * Finds the entry with the greatest length of time since it was last accessed
     * and deletes it.
     * 
     * @return true if we find and successfully delete the last entry; false otherwise
     */
    public boolean removeOldestEntry() {
        long oldestTime = Long.MAX_VALUE;
        String urlToRemove = null;
        
        Enumeration urls = getCachedURLs();
        
        JSONArray entry;
        String url = null;
        long entryLastAccessed;
        try {
            while(urls.hasMoreElements()) {
                url = (String)urls.nextElement();
                entry = cacheIndex.getJSONArray(url);
                entryLastAccessed = entry.getLong(IDX_LASTACCESSED);
                if(entryLastAccessed < oldestTime) {
                    oldestTime = entryLastAccessed;
                    urlToRemove = url;
                }
            }
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 158, url, e);
        }
        
        if(urlToRemove != null) {
            return deleteEntry(urlToRemove);
        }else {
            return false;
        }
    }
    
    
    /**
     * Cache the given HTTPResult as the content of the given URL
     * 
     * @param url
     * @param result
     * @param filename Optional: if specified use the given filename - can help when storing catalogs by id etc.
     * @return 
     */
    public String cacheResult(String url, HTTPResult result, String filename) throws IOException {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        if(getNumEntries() >= maxEntries) {
            removeOldestEntry();
        }
        
        if(filename == null) {
            filename = CatalogController.sanitizeIDForFilename(url);
        }
        
        String extension = '.' + impl.getExtensionFromMimeType(
            result.getHeaderValue("content-type"));
        if(filename  == null && (extension != null && !filename.endsWith(extension))) {
            filename += extension;
        }
        
        String cacheFileURI = UMFileUtil.joinPaths(new String[]{dirName, filename});
        OutputStream out = null;
        IOException ioe = null;
        
        try {
            out = impl.openFileOutputStream(cacheFileURI, 0);
            out.write(result.getResponse());
            out.flush();
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeOutputStream(out);
        }
        
        UMIOUtils.throwIfNotNullIO(ioe);
        
        JSONArray arr  = new JSONArray();

        long currentTime = System.currentTimeMillis();
        long expiresTime = calculateExpiryTime(result.getResponseHeaders());
        
        if(expiresTime == -1) {
            expiresTime = currentTime + DEFAULT_EXPIRES;
        }
        
        try {
            arr.put(IDX_EXPIRES, expiresTime);
            arr.put(IDX_ETAG, result.getHeaderValue("etag"));
            arr.put(IDX_LASTMODIFIED, 0);
            arr.put(IDX_LASTACCESSED, System.currentTimeMillis());
            arr.put(IDX_FILENAME, filename);
            cacheIndex.put(url, arr);
        }catch(JSONException e) {
            //this should never happen putting simlpe values in
            impl.l(UMLog.ERROR, 125, url, e);
        }
        
        
        return cacheFileURI;
    }
    
    public String cacheResult(String url, HTTPResult result) throws IOException{
        return cacheResult(url, result, null);
    }
    
    
    
    
}
