package com.ustadmobile.core.impl;

import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMUtil;

import org.json.JSONObject;

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

    private long lastAccessed;


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
