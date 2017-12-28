package com.ustadmobile.core.impl.http;

import java.util.Hashtable;

/**
 * A wrapper that represents an HTTP request
 */

public class UmHttpRequest {

    private Hashtable headers;

    private String url;

    private String method;

    private boolean onlyIfCached;

    public static final String METHOD_GET = "GET";

    public static final String METHOD_HEAD = "HEAD";

    public static final String METHOD_POST = "POST";

    public static final String HEADER_CACHE_CONTROL = "cache-control";

    public static final String HEADER_CONTENT_TYPE = "content-type";

    public static final String HEADER_CONTENT_LENGTH = "content-length";

    public static final String HEADER_EXPIRES = "expires";

    public static final String HEADER_ETAG = "etag";

    public static final String HEADER_LAST_MODIFIED = "last-modified";

    public static final String CACHE_CONTROL_MUST_REVALIDATE = "must-revalidate";


    /**
     * Create a new HTTP request
     *
     * @param url The Url to load
     */
    public UmHttpRequest(String url) {
        this.url = url;
    }

    /**
     * Create a new HTTP request
     *
     * @param url The Url to load
     * @param headers Hashtable of request headers to use
     */
    public UmHttpRequest(String url, Hashtable headers) {
        this.url = url;
        this.headers = headers;
    }

    /**
     * Gets the url to be retrieved when making this request
     *
     * @return The url to be retrieved
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the request headers to be used with this request
     * @return Hashtable of request headers to be used with this request
     */
    public Hashtable getHeaders() {
        return headers;
    }

    public UmHttpRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public boolean mustRevalidate() {
        if(headers != null && headers.containsKey(HEADER_CACHE_CONTROL)) {
            return ((String)headers.get(HEADER_CACHE_CONTROL)).indexOf("CACHE_CONTROL_MUST_REVALIDATE") != -1;
        }else {
            return false;
        }
    }

    public UmHttpRequest addHeader(String header, String value) {
        if(headers == null)
            headers = new Hashtable();

        headers.put(header, value);
        return this;
    }

    public boolean isOnlyIfCached() {
        return onlyIfCached;
    }

    public UmHttpRequest setOnlyIfCached(boolean onlyIfCached) {
        this.onlyIfCached = onlyIfCached;
        return this;
    }
}
