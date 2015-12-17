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
import java.util.Vector;
/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
    import org.json.*;
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
    
    public static final int DEFAULT_EXPIRES = (1000 * 60 * 60);//1hour
    
    public static final String[] HTTP_MONTH_NAMES = new String[]{"Jan", "Feb",
        "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    
    public HTTPCacheDir(String dirName) {
        this.dirName = dirName;
        
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
            int calYear = Integer.parseInt((String)tokens.elementAt(3));
            
            //Adjust two digit years
            if(calYear < 30) {
                calYear += 2000;
            }else if(calYear < 100) {
                calYear += 1900;
            }
            cal.set(Calendar.YEAR, calYear);
            
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(
                (String)tokens.elementAt(4)));
            cal.set(Calendar.MINUTE, Integer.parseInt(
                (String)tokens.elementAt(5)));
            cal.set(Calendar.SECOND, Integer.parseInt(
                (String)tokens.elementAt(6)));
        }
        
        String toStr = cal.toString();
        return cal != null ? cal.getTime().getTime() : -1;
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
    
    
    /**
     * Get the file URI of a file in the cache if present.  This does not attempt
     * to fetch the url from the Internet in case it's not available.
     * 
     * @param url
     * 
     * @return 
     */
    public String getFileURI(String url) {
        String fileURI = null;
        JSONArray entry = cacheIndex.optJSONArray(url);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        boolean isValid = false;
        
        if (entry != null) {
            try {
                long timeNow = System.currentTimeMillis();
                long entryExpires = entry.getLong(IDX_EXPIRES);

                if (timeNow < entryExpires) {
                    //we can serve it direct from the cache - no validation needed
                    isValid = true;
                } else {
                    //needs to be validated
                    HTTPResult result = impl.makeRequest(url, null, null, "HEAD");
                    String etag = result.getHeaderValue("etag");
                    String cachedEtag = entry.getString(IDX_ETAG);
                    if (etag.equals(cachedEtag)) {
                        isValid = true;
                    }
                }
            } catch (Exception e) {
                impl.l(UMLog.ERROR, 130, url, e);
            }

        }
        
        if(isValid) {
            //Cache hit
            return UMFileUtil.joinPaths(new String[]{ dirName, 
                entry.optString(IDX_FILENAME)});
        }
        
        
        try {
            fileURI = fetch(url);
        }catch(Exception e) {
            impl.l(UMLog.ERROR, 138, url, e);
        }
        
        //fallback in case of being offline - might be stale but better than nothing
        if(fileURI == null && entry != null) {
            fileURI = UMFileUtil.joinPaths(new String[]{ dirName, 
                entry.optString(IDX_FILENAME)});
        }
        
        return fileURI;
    }
    
    
    public String getCacheFileURIByURL(String url) {
        String fileURI = null;
        
        try {
            JSONArray urlEntry = cacheIndex.optJSONArray(url);
            if(urlEntry != null) {
                fileURI = UMFileUtil.joinPaths(new String[] {dirName,
                    urlEntry.optString(IDX_FILENAME)});
            }
        }catch(Exception e) {
            //this should never happen - just digging a value out
            UstadMobileSystemImpl.l(UMLog.ERROR, 121, url, e);
        }
        
        return fileURI;
    }
    
    
    
    public String fetch(String url) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        HTTPResult result = impl.makeRequest(url, null, null);
        String filename = CatalogController.sanitizeIDForFilename(url);
        String extension = '.' + impl.getExtensionFromMimeType(
            result.getHeaderValue("content-type"));
        if(!filename.endsWith(extension)) {
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
        long expiresTime = currentTime + DEFAULT_EXPIRES;
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
    
}
