package com.ustadmobile.core.impl.http


/**
 * A wrapper that represents an HTTP request.
 *
 */
class UmHttpRequest {

    /**
     * Gets the request headers to be used with this request
     * @return Hashtable of request headers to be used with this request
     */
    var headers: MutableMap<String,String>? = null
        private set

    /**
     * Gets the url to be retrieved when making this request
     *
     * @return The url to be retrieved
     */
    var url: String? = null
        private set

    private var method: String? = null

    private var onlyIfCached: Boolean = false

    /**
     * The context object is required for communication with the umDatabase (via HttpCacheDbManager)
     * and to determine http proxy settings.
     *
     * @return System context object.
     */
    var context: Any? = null
        private set

    /**
     * Create a new HTTP request
     *
     * @param url The Url to load
     */
    @Deprecated("")
    constructor(url: String) {
        this.context = context
        this.url = url
    }

    constructor(context: Any, url: String) {
        this.context = context
        this.url = url
        this.method = method
    }


    /**
     * Create a new HTTP request
     *
     * @param url The Url to load
     * @param headers Hashtable of request headers to use
     */
    constructor(url: String, headers: MutableMap<String,String>? = null) {
        this.url = url
        this.headers = headers
    }

    /**
     * Set the method for the request e.g. GET, POST, HEAD etc.
     *
     * @param method
     *
     * @return this request
     */
    fun setMethod(method: String): UmHttpRequest {
        this.method = method
        return this
    }

    fun getMethod(): String {
        return if (method != null) method!! else METHOD_GET
    }

    /**
     * check if this entry requires revalidation
     *
     * @return true if the request must be revalidated (has must-revalidate in the cache-control header), false otherwise
     */
    fun mustRevalidate(): Boolean {
        return if (headers != null && headers!!.containsKey(HEADER_CACHE_CONTROL)) {
            (headers!![HEADER_CACHE_CONTROL] as String).indexOf(CACHE_CONTROL_MUST_REVALIDATE) != -1
        } else {
            false
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
    fun addHeader(header: String, value: String): UmHttpRequest {
        if (headers == null)
            headers = mutableMapOf()

        headers!![header] = value
        return this
    }

    fun isOnlyIfCached(): Boolean {
        return onlyIfCached
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
    fun setOnlyIfCached(onlyIfCached: Boolean): UmHttpRequest {
        this.onlyIfCached = onlyIfCached
        return this
    }

    companion object {

        const val METHOD_GET = "GET"

        const val METHOD_HEAD = "HEAD"

        const val METHOD_POST = "POST"

        const val HEADER_CACHE_CONTROL = "cache-control"

        const val HEADER_CONTENT_TYPE = "content-type"

        const val HEADER_CONTENT_LENGTH = "content-length"

        const val HEADER_EXPIRES = "expires"

        const val HEADER_ETAG = "etag"

        const val HEADER_LAST_MODIFIED = "last-modified"

        const val HEADER_CONTENT_ENCODING = "content-encoding"

        const val CACHE_CONTROL_MUST_REVALIDATE = "must-revalidate"
    }
}
