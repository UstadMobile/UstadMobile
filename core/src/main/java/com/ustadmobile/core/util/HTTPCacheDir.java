/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.util;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Vector;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */

/* $endif$ */


/**
 *
 * Implements a basic HTTP cache dir to save things like image thumbnails etc. to avoid wasting
 * bandwidth and such that they can be used offline
 * 
 * @author mike
 */
public class HTTPCacheDir {

    /**
     * Constant that represents the shared cache: this is used for any cache item that does not have
     * private in the cache-control header
     */
    public static final int SHARED = 0;

    /**
     * Constant that represents the private cache: this is used for any resource that has private
     * in the cache control header
     */
    public static final int PRIVATE = 1;

    /**
     * The number of cache directories that are used here: 2: the shared and the private cache. The
     * private cache can be null when there is no logged in user etc.
     */
    public static final int NUM_DIRS = 2;

    private String[] dirName;
    
    private String[] indexFileURI;
    
    private JSONObject[] cacheIndex;
    
    private static final String INDEX_FILENAME = "cacheindex.json";
    
    public static final int IDX_EXPIRES = 0;
    
    public static final int IDX_ETAG = 1;
    
    public static final int IDX_LASTMODIFIED = 2;
    
    public static final int IDX_LASTACCESSED = 3;
    
    public static final int IDX_FILENAME = 4;
    
    public static final int IDX_MUSTREVALIDATE = 5;

    public static final int IDX_PRIVATE = 6;
    
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

    public static final String CACHE_PRIME_INDEX_RES = "com/ustadmobile/core/cache/";

    private Thread asyncSaveThread = null;
    
    /**
     * Starts a new HTTP cache directory in the directory given
     * 
     * @param sharedDirName Directory to use for caching purposes
     * @param maxEntries the maximum number of entries to be allowed in the cache
     */
    public HTTPCacheDir(String sharedDirName, String privateDirName, int maxEntries) {
        this.dirName = new String[]{sharedDirName, privateDirName};
        indexFileURI = new String[NUM_DIRS];
        cacheIndex = new JSONObject[NUM_DIRS];

        this.maxEntries = maxEntries;
        
        for(int i = 0; i < 2; i++) {
            initCacheDir(i);
        }
    }

    /**
     *
     *
     * @param context
     * @throws IOException
     */
    public void primeCache(Object context) throws IOException{
        InputStream in = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        try {
            in = impl.openResourceInputStream(UMFileUtil.joinPaths(new String[]{CACHE_PRIME_INDEX_RES,
                INDEX_FILENAME}), context);

            if(in == null) {
                ///there is no prime cache prepared
                return;
            }


            UMIOUtils.readFully(in, bout, 1024);
            in.close();
            in = null;

            /* $if umplatform != 2 $ */
            String cacheJsonStr = new String(bout.toByteArray(), "UTF-8");
            JSONObject primeObject = new JSONObject(cacheJsonStr);
            Iterator<String> primeUrls = primeObject.keys();
            String url;
            JSONArray entry;
            while(primeUrls.hasNext()) {
                url = primeUrls.next();
                if(getEntry(url) == null){
                    InputStream entryIn = null;
                    OutputStream entryOut = null;
                    String filename;
                    try {
                        entry = primeObject.getJSONArray(url);
                        filename = entry.getString(IDX_FILENAME);
                        entryIn = impl.openResourceInputStream(UMFileUtil.joinPaths(new String[]{
                            CACHE_PRIME_INDEX_RES, filename}), context);
                        entryOut = impl.openFileOutputStream(UMFileUtil.joinPaths(new String[]{
                                dirName[SHARED], filename}), 0);
                        UMIOUtils.readFully(entryIn, entryOut, 1024);
                        addEntryToCache(url, entry, SHARED);
                    }catch(IOException e) {
                        UstadMobileSystemImpl.l(UMLog.ERROR, 653, url, e);
                    }catch(JSONException j) {
                        UstadMobileSystemImpl.l(UMLog.ERROR, 655, url, j);
                    }finally {
                        UMIOUtils.closeOutputStream(entryOut);
                        UMIOUtils.closeInputStream(entryIn);
                    }
                }
            }
            /* $endif */

        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 654, null, e);
        }finally {
            UMIOUtils.closeInputStream(in);
        }
        saveIndex();
    }
    
    public HTTPCacheDir(String sharedDirName, String privateDirName) {
        this(sharedDirName, privateDirName, DEFAULT_MAX_ENTRIES);
    }

    /**
     *
     * @param privateDirName
     */
    public void setPrivateCacheDir(String privateDirName) {
        //save the private index here if it exists
        dirName[PRIVATE] = privateDirName;
        initCacheDir(PRIVATE);
    }

    /**
     * Initialize the given cache directory:
     *  Make the directory if it does not exist
     *  Load the cache index json if that does exist, otherwise create a blank new json object for it
     *
     * @param cacheNum SHARED or PRIVATE
     */
    protected void initCacheDir(int cacheNum) {
        if(dirName[cacheNum] != null) {
            indexFileURI[cacheNum] = UMFileUtil.joinPaths(new String[]{ dirName[cacheNum],
                    INDEX_FILENAME});
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            try {
                impl.makeDirectoryRecursive(dirName[cacheNum]);
                if(impl.fileExists(indexFileURI[cacheNum])) {
                    cacheIndex[cacheNum] = new JSONObject(impl.readFileAsText(indexFileURI[cacheNum],
                            "UTF-8"));
                }
            }catch(Exception e) {
                impl.l(UMLog.ERROR, 134, indexFileURI[cacheNum], e);
            }

            if(cacheIndex[cacheNum] == null) {
                cacheIndex[cacheNum] = new JSONObject();
            }
        }else {
            indexFileURI[cacheNum] = null;
            cacheIndex[cacheNum] = null;
        }
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
        boolean savedOK = true;

        for(int i = 0; i < 2; i++) {
            impl.l(UMLog.DEBUG, 515, indexFileURI[i]);

            if(indexFileURI[i] == null)
                continue;

            try {
                impl.writeStringToFile(cacheIndex[i].toString(), indexFileURI[i], "UTF-8");
                savedOK = true;
            }catch(Exception e) {
                impl.l(UMLog.ERROR, 152, indexFileURI[i], e);
                savedOK = false;
            }
        }


        
        return savedOK;
    }

    public void saveIndexAsync(){
        if(asyncSaveThread == null) {
            asyncSaveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    saveIndex();
                    asyncSaveThread = null;
                }
            });
            asyncSaveThread.start();
        }
    }
    
    
    private JSONArray getEntryByFilename(String filename) {
        boolean fileExists = false;
        for(int i = 0; i < 2 && !fileExists; i++) {
            try {
                fileExists = UstadMobileSystemImpl.getInstance().fileExists(
                        UMFileUtil.joinPaths(new String[]{dirName[i], filename}));
            }catch(IOException e) {

            }
        }
        
        if(!fileExists) {
            return null;
        }
        
        Enumeration e = getCachedURLs();
        String currentFilename;
        JSONArray currentEntry = null;
        String url;
        while(e.hasMoreElements()) {
            try {
                url = (String)e.nextElement();
                for(int i = 0; i < NUM_DIRS && cacheIndex[i] != null; i++){
                    currentEntry = cacheIndex[i].optJSONArray(url);
                    if(currentEntry != null && filename.equals(currentEntry.getString(IDX_FILENAME))) {
                        return currentEntry;
                    }
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
     * @param headers Option headers to be sent with the request.  Headers should be kept lower case 
     *  If cache-control no-cache is present any cached entry will not be used. If must-revalidate
     *  is present and a cached entry is present it will be revalidated even if it's recent
     * @param resultBuf Optional: An Array of 1 HTTPResult that in case we do wind
     * up directly fetching the result from the network this will be referenced
     * to the HTTPResult object.
     * 
     * @return The file URI path to the cached file
     * 
     * @throws IOException 
     */
    public String get(String url, String filename, Hashtable headers, HTTPResult[] resultBuf) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        boolean isValid = false;
        String dataURL = null;
        boolean noCache = false;
        boolean mustRevalidate = false;
        InputStream resultBufIn = null;

        UstadMobileSystemImpl.l(UMLog.INFO, 383, "Cache GET " + url);

        if(url.startsWith("data:")) {
            dataURL = url;
            url = convertIfDataURL(url);
        }
        
        //check and see if we have the entry
        JSONArray entry = null;
        int cacheNum;

        for(cacheNum = 0; cacheNum < NUM_DIRS && cacheIndex[cacheNum] != null; cacheNum++) {
            entry = cacheIndex[cacheNum].optJSONArray(url);
            if(entry != null)
                break;
        }
        HTTPResult result = null;
        
        //if the filename is being used for an ID - lets see if that exists...
        if(entry == null && filename != null) {
            entry = getEntryByFilename(filename);
            if(entry != null) {
                cacheNum = entry.getBoolean(IDX_PRIVATE) != true ? SHARED : PRIVATE;
            }
        }

        //check that the file is still present in the cache (e.g. Android may have deleted it)
        if(entry != null) {
            String fileUri = UMFileUtil.joinPaths(new String[]{
                    dirName[cacheNum], entry.getString(IDX_FILENAME)});
            if(!(impl.fileExists(fileUri) && impl.fileSize(fileUri) > 0)) {
                //Cache entry has been deleted by someone else
                cacheIndex[cacheNum].remove(url);
                entry = null;
            }
        }
        
        if(headers != null && headers.containsKey("cache-control")) {
            Hashtable cacheCtrl = UMFileUtil.parseParams((String)headers.get("cache-control"), ';');
            noCache = cacheCtrl.containsKey("no-cache");
            mustRevalidate = cacheCtrl.containsKey("must-revalidate");
        }
        
        if (entry != null && !noCache) {
            try {
                long timeNow = System.currentTimeMillis();
                long entryExpires = entry.getLong(IDX_EXPIRES);
                Hashtable responseHeaders = null;

                if (timeNow < entryExpires && !mustRevalidate) {
                    //we can serve it direct from the cache - no validation needed
                    isValid = true;
                    UstadMobileSystemImpl.l(UMLog.INFO, 385, "Cache HIT (no validation)" + url);
                    impl.l(UMLog.DEBUG, 606, url);
                    responseHeaders = new Hashtable();
                } else if(dataURL == null){
                    //needs to be validated
                    result = impl.makeRequest(url, headers, null, "HEAD");
                    isValid = validateCacheEntry(entry, result);
                    if(isValid) {
                        UstadMobileSystemImpl.l(UMLog.INFO, 385, "Cache HIT (validated)" + url);
                        entry.put(IDX_EXPIRES, calculateExpiryTime(result.getResponseHeaders(),
                                System.currentTimeMillis() + DEFAULT_EXPIRES));
                        responseHeaders = result.getResponseHeaders();
                    }


                    impl.l(UMLog.DEBUG, 607, url + ':' + isValid);
                }
                
                if(isValid) {
                    entry.put(IDX_LASTACCESSED, timeNow);
                    String fileUri = UMFileUtil.joinPaths(new String[]{ dirName[cacheNum],
                            entry.optString(IDX_FILENAME)});

                    if(resultBuf != null) {
                        resultBufIn = impl.openFileInputStream(fileUri);
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        UMIOUtils.readFully(resultBufIn, bout, 8 * 1024);

                        resultBuf[0] = new HTTPResult(bout.toByteArray(), 200, responseHeaders);
                    }

                    return fileUri;
                }
            } catch (Exception e) {
                impl.l(UMLog.ERROR, 130, url, e);
            }

        }
        
        try {
            if(dataURL == null) {
                result = impl.makeRequest(url, headers, null);
                UstadMobileSystemImpl.l(UMLog.INFO, 386, "Cache MISS " + url);
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
        
        //Looks like we are offline... use anything we have unless no-cache / must-revalidate are set
        if(entry != null && !noCache && !mustRevalidate) {
            String fileUri = UMFileUtil.joinPaths(new String[]{ dirName[cacheNum], entry.optString(
                    IDX_FILENAME)});
            if(resultBuf != null) {
                resultBufIn = impl.openFileInputStream(fileUri);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                UMIOUtils.readFully(resultBufIn, bout, 8 * 1024);

                resultBuf[0] = new HTTPResult(bout.toByteArray(), 200, new Hashtable());
            }

            return fileUri;
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
        return get(url, filename, null, null);
    }
    
    /**
     * 
     * @param url
     * @return
     * @throws IOException 
     */
    public String get(String url) throws IOException{
        return get(url, null, null, null);
    }
    
    /**
     * Gets the number of entries in the cache
     * 
     * @return Number of entries in the cache
     */
    public int getNumEntries() {
        int count = 0;
        for(int i = 0; i < NUM_DIRS; i++) {
            if(cacheIndex[i] == null)
                continue;

            count += cacheIndex[i].length();
        }
        return count;
    }

    /**
     * Get the JSON array that represents the cached object
     *
     * @param url The URL to lookup
     * 
     * @return JSONArray with values as per IDX_* constants
     */
    public JSONArray getEntry(String url) {
        JSONArray cacheEntry = null;
        try {
            for(int i = 0; i < NUM_DIRS && cacheEntry == null; i++) {
                cacheEntry = cacheIndex[i].optJSONArray(convertIfDataURL(url));
            }
        }catch(Exception e) {
            //this really would never happen - using the opt method on a prebuilt jsonobject
        }
        return cacheEntry;
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
        int cacheNum;
        try {
            for(cacheNum = 0; cacheNum < NUM_DIRS && !fileExists; cacheNum++) {
                fileURI = UMFileUtil.joinPaths(new String[]{dirName[cacheNum], filename});
                fileURI = UMFileUtil.joinPaths(new String[]{dirName[cacheNum], filename});
                fileExists = impl.fileExists(fileURI);
            }


        }catch(IOException e) {
            //TODO: Need to log this
        }
        
        return fileExists ? fileURI : null;
    }
    
    public String getCacheFileURIByURL(String url) {
        String fileURI = null;
        
        try {
            JSONArray urlEntry = null;
            int cacheNum;
            for(cacheNum = 0; cacheNum < NUM_DIRS && cacheIndex[cacheNum] != null; cacheNum++) {
                urlEntry = cacheIndex[cacheNum].optJSONArray(convertIfDataURL(url));
                if(urlEntry != null)
                    break;
            }

            if(urlEntry != null) {
                urlEntry.put(IDX_LASTACCESSED, System.currentTimeMillis());
                fileURI = UMFileUtil.joinPaths(new String[] {dirName[cacheNum],
                    urlEntry.optString(IDX_FILENAME)});
            }
        }catch(Exception e) {
            //this should never happen - just digging a value out
            UstadMobileSystemImpl.l(UMLog.ERROR, 121, url, e);
        }
        
        return fileURI;
    }
    
    public boolean hasCachedURL(String url) {
        boolean foundEntry = false;
        for(int i = 0; i < NUM_DIRS && !foundEntry; i++) {
            foundEntry = cacheIndex[i].has(convertIfDataURL(url));
        }

        return foundEntry;
    }
    
    public boolean hasCachedFilename(String filename) {
        boolean exists = false;
        try {
            for(int i = 0; i < NUM_DIRS && !exists; i++) {
                exists = UstadMobileSystemImpl.getInstance().fileExists(
                        UMFileUtil.joinPaths(new String[]{dirName[i], filename}));
            }
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
     * @param defaultVal Expiry value to use in case headers do not contain max-age or expires
     *
     * @return 
     */
    public static long calculateExpiryTime(Hashtable headers, long defaultVal) {
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
        
        return defaultVal;
    }

    public static long calculateExpiryTime(Hashtable headers) {
        return calculateExpiryTime(headers, -1);
    }
    
    /**
     * Remove the given entry from the cache
     * 
     * @param url The URL of the item to remove from the cache
     * @return 
     */
    public boolean deleteEntry(String url) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        boolean foundInCache = false;
        boolean deleteResult = true;
        try {
            JSONArray item;
            int cacheNum;
            boolean itemDeleted;
            for(cacheNum = 0; cacheNum < NUM_DIRS && cacheIndex[cacheNum] != null; cacheNum++) {
                item = cacheIndex[cacheNum].optJSONArray(url);
                if(item != null) {
                    foundInCache = true;
                    String fileURI = UMFileUtil.joinPaths(new String[] {dirName[cacheNum],
                            item.getString(IDX_FILENAME)});
                    itemDeleted = impl.removeFile(fileURI);
                    //NOTE: If the file was somehow already removed - that's still OK
                    deleteResult &= !impl.fileExists(fileURI);
                    if(itemDeleted) {
                        cacheIndex[cacheNum].remove(url);
                    }
                }
            }
        }catch(Exception e) {
            impl.l(UMLog.ERROR, 156, url, e);
        }
        
        return foundInCache && deleteResult;
    }
    
    /**
     * Make an enumeration of all the urls that are in the cache
     * 
     * @return 
     */
    public Enumeration getCachedURLs() {
        Enumeration urls;
        /* $if umplatform == 2  $
            //TODO: Support both private and shared cache values in getCachedUrls on J2ME. For now return only those in shared cache.
            urls = cacheIndex[SHARED].keys();
         $else$ */
            final Iterator[] urlIterators = new Iterator[NUM_DIRS];
            for(int i = 0; i < NUM_DIRS; i++) {
                if(cacheIndex[i] == null)
                    continue;

                urlIterators[i] = cacheIndex[i].keys();
            }

            urls = new IteratorArrayEnumeration(urlIterators);
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
        int cacheNum;
        try {
            while(urls.hasMoreElements()) {
                entry = null;
                url = (String)urls.nextElement();
                for(cacheNum = 0; cacheNum < NUM_DIRS && entry == null; cacheNum++) {
                    entry = cacheIndex[cacheNum].getJSONArray(url);
                }

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
     *
     * @return 
     */
    public String cacheResult(String url, HTTPResult result, String filename) throws IOException {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        if(getNumEntries() >= maxEntries) {
            removeOldestEntry();
        }
        
        if(filename == null) {
            filename = CatalogPresenter.sanitizeIDForFilename(url);
        }
        
        String contentType = result.getHeaderValue("content-type");

        int sepIndex = contentType != null ? contentType.indexOf(';') : -1;
        if(sepIndex != -1) {
            contentType = contentType.substring(0, sepIndex).trim();
        }

        if(contentType != null) {
            String extension = '.' + impl.getExtensionFromMimeType(contentType);
            if(filename  == null && (extension != null && !filename.endsWith(extension))) {
                filename += extension;
            }
        }


        String cacheControlHeader = result.getHeaderValue("cache-control");
        int cacheNum = (cacheControlHeader != null && cacheControlHeader.indexOf("private") != -1)
            ? PRIVATE: SHARED;

        //if the file is private and there is no private cache directory active
        if(cacheNum == PRIVATE && dirName[PRIVATE] == null) {
            return null;
        }

        String cacheFileURI = UMFileUtil.joinPaths(new String[]{dirName[cacheNum], filename});
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
        


        long currentTime = System.currentTimeMillis();
        long expiresTime = calculateExpiryTime(result.getResponseHeaders(),
                currentTime + DEFAULT_EXPIRES);

        try {
            JSONArray cacheEntry = new JSONArray();
            cacheEntry.put(IDX_EXPIRES, expiresTime);
            cacheEntry.put(IDX_ETAG, result.getHeaderValue("etag"));
            cacheEntry.put(IDX_LASTMODIFIED, 0);
            cacheEntry.put(IDX_LASTACCESSED, System.currentTimeMillis());
            cacheEntry.put(IDX_FILENAME, filename);
            cacheEntry.put(IDX_PRIVATE, cacheNum == PRIVATE);
            addEntryToCache(url, cacheEntry, cacheNum);
        }catch(JSONException e) {
            //this should never happen putting simlpe values in
            impl.l(UMLog.ERROR, 125, url, e);
        }

        return cacheFileURI;
    }

    public String cacheResult(String url, HTTPResult result) throws IOException{
        return cacheResult(url, result, null);
    }


    protected void addEntryToCache(String url, JSONArray entry, int cacheNum) {
        try {
            cacheIndex[cacheNum].put(url, entry);
        }catch(JSONException e) {
            //should never happen - putting a basic key in
            e.printStackTrace();
        }
    }


    /* $if umplatform != 2 $ */
    static class IteratorArrayEnumeration implements Enumeration{

        private int iteratorIndex;

        private Iterator[] iterators;

        IteratorArrayEnumeration(Iterator[] iterators) {
            this.iterators = iterators;
            this.iteratorIndex = 0;
        }

        public boolean hasMoreElements(int index) {
            if(iterators[index] != null && iterators[index].hasNext()) {
                return true;
            }else if((index + 1) < iterators.length) {
                return this.hasMoreElements(index+1);
            }else{
                return false;
            }
        }

        public boolean hasMoreElements() {
            return hasMoreElements(iteratorIndex);
        }

        public Object nextElement() {
            if(iterators[iteratorIndex] != null && iterators[iteratorIndex].hasNext()) {
                return iterators[iteratorIndex].next();
            }else if(iteratorIndex < iterators.length) {
                iteratorIndex++;
                return this.nextElement();
            }else {
                throw new RuntimeException ("No more elements");
            }
        }
    }

    /* $endif$ */
    
}
