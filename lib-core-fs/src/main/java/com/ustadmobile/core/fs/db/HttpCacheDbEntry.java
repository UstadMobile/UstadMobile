package com.ustadmobile.core.fs.db;

/**
 * ORM interface representing the fields to be stored by the underlying database for a cache entry.
 */

public interface HttpCacheDbEntry {

    //Primary key
    int getEntryId();

    void setEntryId(int entryId);

    String getUrl();

    void setUrl(String url);

    long getExpiresTime();

    void setExpiresTime(long expiresTime);

    String geteTag();

    void seteTag(String eTag);

    long getLastModified();

    void setLastModified(long lastModified);

    String getContentType();

    void setContentType(String contentType);

    long getContentLength();

    void setContentLength(long contentLength);

    String getCacheControl();

    void setCacheControl(String cacheControl);

    int getStatusCode();

    void setStatusCode(int statusCode);

    String getFileUri();

    void setFileUri(String fileUri);

    long getLastAccessed();

    void setLastAccessed(long lastAccessed);

    long getLastChecked();

    void setLastChecked(long lastChecked);

}
