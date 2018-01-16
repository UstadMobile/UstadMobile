package com.ustadmobile.core.impl;

import com.ustadmobile.core.fs.db.HttpCacheDbEntry;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMFileUtil;

import java.util.Hashtable;

/**
 * Represents an entry in the cache.
 *
 * The property getters and setters are delegated to an platform dependent implementation of
 * HttpCacheDbEntry (to be found using HttpCacheDbManager on the implementing platform).
 *
 * Created by mike on 12/26/17.
 */
public class HttpCacheEntry  {

    private HttpCacheDbEntry dbProxy;

    static final String CACHE_CONTROL_KEY_MAX_AGE = "max-age";

    /**
     * The default time for which a cache entry is considered fresh from the time it was last checked
     * if the server does not provide this information using the cache-control or expires header.
     */
    public static final int DEFAULT_TIME_TO_LIVE = (60 * 60 * 1000);


    public HttpCacheEntry(HttpCacheDbEntry dbProxy) {
        this.dbProxy = dbProxy;
    }

    public void updateFromResponse(UmHttpResponse networkResponse) {
        String headerVal;
        if(networkResponse.getStatus() != 304) {
            //new entry was downloaded - update the length etc.
            headerVal = networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_LENGTH);
            if(headerVal != null) {
                try {
                    setContentLength(Integer.parseInt(headerVal));
                }catch(IllegalArgumentException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 74, headerVal, e);
                }
            }

            setStatusCode(networkResponse.getStatus());
        }

        setCacheControl(networkResponse.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL));
        setContentType(networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE));
        setExpiresTime(convertDateHeaderToLong(UmHttpRequest.HEADER_EXPIRES, networkResponse));
        setContentType(networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE));
        seteTag(networkResponse.getHeader(UmHttpRequest.HEADER_ETAG));
        setCacheControl(networkResponse.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL));
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
        return dbProxy.getExpiresTime();
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
        String cacheControl = dbProxy.getCacheControl();
        if(cacheControl != null) {
            Hashtable ccParams = UMFileUtil.parseParams(cacheControl, ',');
            if(ccParams.containsKey(CACHE_CONTROL_KEY_MAX_AGE)) {
                long maxage = Integer.parseInt((String)ccParams.get(CACHE_CONTROL_KEY_MAX_AGE));
                return dbProxy.getLastChecked() + (maxage * 1000);
            }
        }

        if(dbProxy.getExpiresTime() >= 0) {
            return dbProxy.getExpiresTime();
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

        if(expiryTime != -1) {
             return expiryTime > timeNow;
        }else {
            return dbProxy.getLastChecked() + timeToLive > timeNow;
        }
    }

    public HttpCacheDbEntry getDbEntry() {
        return dbProxy;
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


    public int getEntryId() {
        return dbProxy.getEntryId();
    }

    public void setEntryId(int entryId) {
        dbProxy.setEntryId(entryId);
    }

    public String getUrl() {
        return dbProxy.getUrl();
    }

    public void setUrl(String url) {
        dbProxy.setUrl(url);
    }

    public void setExpiresTime(long expiresTime) {
        dbProxy.setExpiresTime(expiresTime);
    }

    public String geteTag() {
        return dbProxy.geteTag();
    }

    public void seteTag(String eTag) {
        dbProxy.seteTag(eTag);
    }

    public long getLastModified() {
        return dbProxy.getLastModified();
    }

    public void setLastModified(long lastModified) {
        dbProxy.setLastModified(lastModified);
    }

    public String getContentType() {
        return dbProxy.getContentType();
    }

    public void setContentType(String contentType) {
        dbProxy.setContentType(contentType);
    }

    public long getContentLength() {
        return dbProxy.getContentLength();
    }

    public void setContentLength(long contentLength) {
        dbProxy.setContentLength(contentLength);
    }

    public String getCacheControl() {
        return dbProxy.getCacheControl();
    }

    public void setCacheControl(String cacheControl) {
        dbProxy.setCacheControl(cacheControl);
    }

    public int getStatusCode() {
        return dbProxy.getStatusCode();
    }

    public void setStatusCode(int statusCode) {
        dbProxy.setStatusCode(statusCode);
    }

    public String getFileUri() {
        return dbProxy.getFileUri();
    }

    public void setFileUri(String fileUri) {
        dbProxy.setFileUri(fileUri);
    }

    public long getLastAccessed() {
        return dbProxy.getLastAccessed();
    }

    public void setLastAccessed(long lastAccessed) {
        dbProxy.setLastAccessed(lastAccessed);
    }

    public long getLastChecked() {
        return dbProxy.getLastChecked();
    }

    public void setLastChecked(long lastChecked) {
        dbProxy.setLastChecked(lastChecked);
    }

}
