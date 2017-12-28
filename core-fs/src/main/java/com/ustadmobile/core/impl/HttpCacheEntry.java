package com.ustadmobile.core.impl;

import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMUtil;

import org.json.JSONObject;

import java.util.Hashtable;

/**
 * Created by mike on 12/26/17.
 */

public class HttpCacheEntry {

    private String url;

    private String fileUri;

    private long expiresTime;

    private String contentType;

    private String eTag;

    private long lastModified;

    private String cacheControl;

    private long contentLength;

    private int statusCode;

    private long lastChecked;

    static final String JSON_KEY_URL = "url";

    static final String JSON_KEY_FILE_URI = "file";

    static final String JSON_KEY_STATUS_CODE = "status";

    static final String JSON_KEY_LAST_ACCESSED = "la";

    static final String JSON_KEY_LAST_CHECKED = "lc";

    static final String CACHE_CONTROL_KEY_MAX_AGE = "max-age";

    private long lastAccessed;

    /**
     * The default time for which a cache entry is considered fresh from the time it was last checked
     * if the server does not provide this information using the cache-control or expires header.
     */
    public static final int DEFAULT_TIME_TO_LIVE = (60 * 60 * 1000);


    public HttpCacheEntry() {

    }

    protected void loadFromJson(JSONObject json) {
        url = json.optString(JSON_KEY_URL);
        fileUri = json.optString(JSON_KEY_FILE_URI);
        expiresTime = json.optLong(UmHttpRequest.HEADER_EXPIRES, 0L);
        contentType = json.optString(UmHttpRequest.HEADER_CONTENT_TYPE);
        eTag = json.optString(UmHttpRequest.HEADER_ETAG);
        lastModified = json.optLong(UmHttpRequest.HEADER_LAST_MODIFIED, 0L);
        cacheControl = json.optString(UmHttpRequest.HEADER_CACHE_CONTROL);
        contentLength = json.optLong(UmHttpRequest.HEADER_CONTENT_LENGTH, -1);
        statusCode = json.optInt(JSON_KEY_STATUS_CODE, 0);
        lastAccessed = json.optLong(JSON_KEY_LAST_ACCESSED, 0);
        lastChecked = json.optLong(JSON_KEY_LAST_CHECKED);
    }

    public JSONObject toJson() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(JSON_KEY_URL, url);
        jsonObj.put(JSON_KEY_FILE_URI, fileUri);
        jsonObj.put(UmHttpRequest.HEADER_EXPIRES, expiresTime);
        jsonObj.put(UmHttpRequest.HEADER_CONTENT_TYPE, contentType);
        jsonObj.put(UmHttpRequest.HEADER_ETAG, eTag);
        jsonObj.put(UmHttpRequest.HEADER_CACHE_CONTROL, cacheControl);
        jsonObj.put(UmHttpRequest.HEADER_CONTENT_LENGTH, contentLength);
        jsonObj.put(JSON_KEY_STATUS_CODE, statusCode);
        jsonObj.put(JSON_KEY_LAST_ACCESSED, lastAccessed);
        jsonObj.put(JSON_KEY_LAST_CHECKED, lastChecked);
        return jsonObj;
    }

    public void updateFromResponse(UmHttpResponse response) {
        String headerVal;
        if(response.getStatus() != 304) {
            //new entry was downloaded - update the length etc.
            headerVal = response.getHeader(UmHttpRequest.HEADER_CONTENT_LENGTH);
            if(headerVal != null) {
                try {
                    setContentLength(Integer.parseInt(headerVal));
                }catch(IllegalArgumentException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 74, headerVal, e);
                }
            }

            setStatusCode(response.getStatus());
        }

        setCacheControl(response.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL));
        setContentType(response.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE));
        setExpiresTime(convertDateHeaderToLong(UmHttpRequest.HEADER_EXPIRES, response));
        setContentType(response.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE));
        seteTag(response.getHeader(UmHttpRequest.HEADER_ETAG));
        setCacheControl(response.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL));
    }

    private long convertDateHeaderToLong(String headerName, UmHttpResponse response) {
        String headerVal = response.getHeader(headerName);
        if(headerVal != null) {
            try {
                return UMCalendarUtil.parseHTTPDate(headerVal);
            }catch(NumberFormatException e) {
                return -1L;
            }
        }else {
            return -1L;
        }
    }

    public long getExpiresTime() {
        return expiresTime;
    }

    /**
     * Calculates when an entry will expire based on it's HTTP headers: specifically
     * the expires header and cache-control header
     *
     * As per :  http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html section
     * 14.9.3 the max-age if present will take precedence over the expires header
     *
     * @return -1 if the expiration time calculated from the headers provided if possible, -1 otherwise
     */
    public final long calculateEntryExpirationTime() {
        if(cacheControl != null) {
            Hashtable ccParams = UMFileUtil.parseParams(cacheControl, ',');
            if(ccParams.containsKey(CACHE_CONTROL_KEY_MAX_AGE)) {
                long maxage = Integer.parseInt((String)ccParams.get(CACHE_CONTROL_KEY_MAX_AGE));
                return lastChecked + (maxage * 1000);
            }
        }

        if(expiresTime >= 0) {
            return expiresTime;
        }

        return -1;
    }

    /**
     * Determine if the entry is considered fresh.
     *
     * @see #calculateEntryExpirationTime()
     *
     * @param timeToLive the time since when this entry was last checked for which the entry will be
     *                   considered fresh if the cache-control headers and expires headers do not
     *                   provide this information.
     *
     * @return true if the entry is considered fresh, false otherwise
     */
    public boolean isFresh(int timeToLive) {
        long expiryTime = calculateEntryExpirationTime();
        long timeNow = System.currentTimeMillis();
        if(expiryTime != -1 && expiryTime > timeNow) {
            return true;
        }else {
            return lastChecked + timeToLive > timeNow;
        }
    }

    /**
     * Determine if the entry is considered fresh. This simply calls isFresh with the
     * DEFAULT_TIME_TO_LIVE as the timeToLive parameter.
     *
     * @see #isFresh(int)
     *
     * @return true if the entry is considered fresh, false otherwise
     */
    public boolean isFresh() {
        return isFresh(DEFAULT_TIME_TO_LIVE);
    }

    public void setExpiresTime(long expiresTime) {
        this.expiresTime = expiresTime;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }


    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }
}
