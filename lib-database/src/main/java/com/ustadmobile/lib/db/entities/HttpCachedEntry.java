package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Entity representing an HTTP cache entry
 *
 */
@UmEntity
public class HttpCachedEntry {

    public static final int METHOD_GET = 0;

    public static final int METHOD_POST = 1;

    public static final int METHOD_HEAD = 2;

    @UmPrimaryKey(autoIncrement = true)
    private Integer uid;

    @UmIndexField
    private String url;

    private String fileUri;

    private int method;

    private long expiresTime = -1;

    private String contentType;

    private String etag;

    private long lastModified;

    private String cacheControl;

    private long contentLength;

    private int statusCode;

    private long lastChecked;

    @UmIndexField
    private long lastAccessed;


    static final String CACHE_CONTROL_KEY_MAX_AGE = "max-age";

    /**
     * The default time for which a cache entry is considered fresh from the time it was last checked
     * if the server does not provide this information using the cache-control or expires header.
     */
    public static final int DEFAULT_TIME_TO_LIVE = (60 * 60 * 1000);


    public HttpCachedEntry() {

    }


    /**
     * Primary (artificial) key
     *
     * @return primary key
     */
    public Integer getUid() {
        return uid;
    }

    /**
     * Set the primary key - only to be used by the ORM
     * @param uid
     */
    public void setUid(Integer uid) {
        this.uid = uid;
    }

    /**
     * Get the URL that this cache object represents
     *
     * @return The URL this cache object represents
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the URL the cache object represents.
     *
     * @param url The URL this cache objects represents
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The file URI where the data of this entry is stored
     *
     * @return The file URI where the data of this entry is stored
     */
    public String getFileUri() {
        return fileUri;
    }

    /**
     * Set the file URI where the data of this entry is stored
     * @param fileUri
     */
    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    /**
     * The HTTP method that this entry represents. The cache response varies by HTTP method. Use the
     * METHOD_ flags
     *
     * @return The method used to retrieve this cache response.
     */
    public int getMethod() {
        return method;
    }

    /**
     * Set the HTTP method that this entry represents.
     *
     * @param method The HTTP method that this cached entry represents
     */
    public void setMethod(int method) {
        this.method = method;
    }

    /**
     * The calculated expires time of this entry (in unix time)
     *
     * @return The calculated expires time of this entry
     */
    public long getExpiresTime() {
        return expiresTime;
    }

    /**
     * Set the calculated expires time of this entry
     *
     * @param expiresTime The calculated expires time for this entry
     */
    public void setExpiresTime(long expiresTime) {
        this.expiresTime = expiresTime;
    }

    /**
     * The content type from the HTTP response
     *
     * @return The HTTP content type header value
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set the http content header value
     *
     * @param contentType The HTTP content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Getter for the etag
     *
     * @return The HTTP ETAG header (if known)
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Setter for the etag property
     *
     * @param etag The HTTP ETAG header (null if not present)
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Getter for the last modified property
     *
     * @return The last modified time (parsed from the HTTP last-modified header)
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Setter for the last modified property
     *
     * @param lastModified The last modified time (parsed from the HTTP last-modified header)
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Getter for the cache control property
     *
     * @return The cache-control header as it was provided by the server, null if none was provided
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Setter for the cache control property
     *
     * @param cacheControl The cache-control header as it was provided by the server, null if none was provided
     */
    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * Getter for the content length property
     *
     * @return The content-length header as it was provided by the server
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Setter for the content length property
     *
     * @param contentLength The content-length header as it was provided by the server
     */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Getter for the status code property
     *
     * @return The HTTP status code as it was provided by the server.
     */
    public int getStatusCode() {
        return statusCode;
    }


    /**
     * Setter for the status code property
     *
     * @param statusCode The HTTP status code as it was provided by the server.
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Getter for the last checked property
     *
     * @return The time, in unix time, that this entry was last checked against the server
     */
    public long getLastChecked() {
        return lastChecked;
    }

    /**
     * Setter for the last checked property
     *
     * @param lastChecked The time, in unix time, that this entry was last checked against the server
     */
    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }

    /**
     * Getter for the last accessed property
     *
     * @return The time that this entry was last accessed in a request (used to prioritize retention / deletion)
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    /**
     * Setter for the last accessed property
     *
     * @param lastAccessed The time that this entry was last accessed in a request (used to prioritize retention / deletion)
     */
    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
}
