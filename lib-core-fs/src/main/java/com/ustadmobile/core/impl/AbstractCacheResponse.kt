package com.ustadmobile.core.impl

import com.ustadmobile.core.impl.http.UmHttpResponse

/**
 * An HttpResponse provided by the cache. All UmHttpResponse objects provided by HttpCache are
 * descendants of this class.
 *
 * Created by mike on 12/27/17.
 */
abstract class AbstractCacheResponse : UmHttpResponse() {

    /**
     * Determine if the item was returned from the network or from the cache.
     *
     * @see .MISS
     *
     * @see .HIT_DIRECT
     *
     * @see .HIT_VALIDATED
     *
     *
     * @return Flag indicating whether the respnose is from the cache or the network
     */
    var cacheResponse: Int = 0

    /**
     * Indicates that this response is from the network, where the network server replied 304. Thus
     * if something has already been displayed using a request set to onlyIfCached, that response is
     * accurate is still valid and nothing needs to be changed/done.
     *
     * @return boolean indicating if this is a 304 response
     */
    var isNetworkResponseNotModified: Boolean = false

    /**
     * Determine if the response is considered a hit or a miss.
     *
     * @return true if the response was a cache hit, false otherwise
     */
    val isHit: Boolean
        get() = cacheResponse == HIT_DIRECT || cacheResponse == HIT_VALIDATED

    /**
     * Get the file path to where this entry is stored on disk.
     *
     * @return File path to where this entry is stored on disk.
     */
    abstract val fileUri: String

    abstract val isFresh: Boolean

    /**
     * Indicates if the cache entry is fresh. The entry
     * @param timeToLive
     * @return
     */
    abstract fun isFresh(timeToLive: Int): Boolean

    companion object {

        /**
         * Represents a respnose that was served entirely from the network.
         */
        val MISS = 0

        /**
         * Represents a response that was served directly from the cache without any http traffic
         * required. It was either within the default period that it was considered fresh, or the original
         * cache control / expiry information was sufficient.
         */
        val HIT_DIRECT = 1

        /**
         * Represents a response that was served from the cache, but needed an http trip to validate
         * the response.
         */
        val HIT_VALIDATED = 2
    }

}
