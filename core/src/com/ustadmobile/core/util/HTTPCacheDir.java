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
    
    public static final int[] HTTP_DAYS = new int[]{ Calendar.MONDAY, Calendar.TUESDAY,
        Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,
        Calendar.SUNDAY
    };
    
    public static final String[] HTTP_DAY_LABELS = new String[]{"Mon", "Tue",
        "Wed", "Thu", "Fri", "Sat", "Sun"};
    
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
    
    
    public String get(String url) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        boolean isValid = false;
        
        //check and see if we have the entry
        JSONArray entry = cacheIndex.optJSONArray(url);
        HTTPResult result = null;
        
        if (entry != null) {
            try {
                long timeNow = System.currentTimeMillis();
                long entryExpires = entry.getLong(IDX_EXPIRES);

                if (timeNow < entryExpires) {
                    //we can serve it direct from the cache - no validation needed
                    isValid = true;
                } else {
                    //needs to be validated
                    result = impl.makeRequest(url, null, null, "HEAD");
                    isValid = validateCacheEntry(entry, result);
                }
                
                if(isValid) {
                    return UMFileUtil.joinPaths(new String[]{ dirName, 
                        entry.optString(IDX_FILENAME)});
                }
            } catch (Exception e) {
                impl.l(UMLog.ERROR, 130, url, e);
            }

        }
        
        
        result = impl.makeRequest(url, null, null);
        return cacheResult(url, result);
    }
    
    public boolean validateCacheEntry(JSONArray entry, HTTPResult result) {
        boolean isValid = false;
        try {
            String validateHeader = result.getHeaderValue("etag");
            String cachedEtag = entry.getString(IDX_ETAG);
            if (validateHeader.equals(cachedEtag)) {
                isValid = true;
            }else if((validateHeader = result.getHeaderValue("Last-Modified")) != null){
                long cachedLastModified = entry.getLong(IDX_LASTMODIFIED);
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
    
    /**
     * Cache the given HTTPResult as the content of the given URL
     * 
     * @param url
     * @param result
     * @return 
     */
    public String cacheResult(String url, HTTPResult result) throws IOException {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
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
