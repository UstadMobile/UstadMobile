package com.ustadmobile.core.impl.http;



import java.util.Hashtable;

/**
 * A wrapper that represents an HTTP request.
 *
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

    public static final String HEADER_CONTENT_ENCODING = "content-encoding";

    public static final String CACHE_CONTROL_MUST_REVALIDATE = "must-revalidate";

    private Object context;

    /**
     * Create a new HTTP request
     *
     * @param url The Url to load
     */
    @Deprecated
    public UmHttpRequest(String url) {
        this.context = context;
        this.url = url;
    }

    public UmHttpRequest(Object context, String url) {
        this.context = context;
        this.url = url;
        this.method = method;
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

    /**
     * Set the method for the request e.g. GET, POST, HEAD etc.
     *
     * @param method
     *
     * @return this request
     */
    public UmHttpRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getMethod() {
        return method != null ? method : METHOD_GET;
    }

    /**
     * check if this entry requires revalidation
     *
     * @return true if the request must be revalidated (has must-revalidate in the cache-control header), false otherwise
     */
    public boolean mustRevalidate() {
        if(headers != null && headers.containsKey(HEADER_CACHE_CONTROL)) {
            return ((String)headers.get(HEADER_CACHE_CONTROL)).indexOf(CACHE_CONTROL_MUST_REVALIDATE) != -1;
        }else {
            return false;
        }
    }

    /**
     * Add a header to this request
     *
     * @param header The header to send
     * @param value The value of the header
     *
     * @return this request
     */
    public UmHttpRequest addHeader(String header, String value) {
        if(headers == null)
            headers = new Hashtable();

        headers.put(header, value);
        return this;
    }

    public boolean isOnlyIfCached() {
        return onlyIfCached;
    }

    /**
     * Sometimes for performance reasons one might want to show something only if the cached entry
     * is available, for example when a view is initially shown, images etc. from the cache can be
     * shown immediately before images are loaded and/or validated against the network.
     *
     * If onlyIfCached is set to true, there will be no network request made and only data from the
     * cache will be returned. It is possible to determine if the cached reply is fresh (in which
     * case no network request is needed) by using HttpResponse.isFresh . If the response is cached,
     * but the headers do not provide sufficient information for validation, then the network response
     * might be 304 not modified, in which case no updateState to cached content on the view would be
     * needed, using AbstractCacheResponse.isNetworkResponseNotModified .
     *
     * @param onlyIfCached
     *
     * @return this response
     */
    public UmHttpRequest setOnlyIfCached(boolean onlyIfCached) {
        this.onlyIfCached = onlyIfCached;
        return this;
    }

    /**
     * The context object is required for communication with the database (via HttpCacheDbManager)
     * and to determine http proxy settings.
     *
     * @return System context object.
     */
    public Object getContext() {
        return context;
    }
}
