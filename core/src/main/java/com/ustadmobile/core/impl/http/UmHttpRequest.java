package com.ustadmobile.core.impl.http;

import java.util.Hashtable;

/**
 * A wrapper that represents an HTTP request
 */

public class UmHttpRequest {

    private Hashtable headers;

    private String url;

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

}
