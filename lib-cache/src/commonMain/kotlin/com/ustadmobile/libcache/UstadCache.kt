package com.ustadmobile.libcache

import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.response.HttpResponse
import kotlinx.io.Source

data class EntryLockRequest(
    val url: String,
    val remark: String = "",
)


/**
 * ===Content===
 *
 * Done within ContentJob:
 *
 * Uploaded on server:
 *
 * a) go through files: generate sitemap, cache it as http(s)://endpoint/contententry/version/sitemap.xml
 * b) cache all files as http(s)://endpoint/api/contententry/version/file/path
 * c) add a retention lock for all cached entries (linked to ContentEntryVersion)
 * d) Create ContentEntryVersion entity
 * e) ktor endpoint will serve by using the cache.
 *
 * Uploaded on Android:
 *
 * Follow a-d above.
 * e) upload data to server, replicate ContentEntryVersion to server, release lock
 */
@Suppress("unused")
interface UstadCache {

    /**
     * CacheListener is not normally required, but can be needed in tests to wait for
     * caching a request to be completed.
     */
    interface CacheListener {

        fun onEntriesStored(storeRequest: List<CacheEntryToStore>)

    }

    /**
     * Store a set of requests with their corresponding response.
     */
    fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener? = null,
    ): List<StoreResult>

    /**
     * Store all entries from a given Zip as entries in the cache. This is useful to process zipped
     * content e.g. epubs, xAPI/Scorm files, etc.
     *
     * @param zipSource Source for Zip data
     * @param urlPrefix should end with /
     * @param retain true if entries should be marked as to retain
     * @param static true if entries will have the Coupon-Static: true header
     */
    fun storeZip(
        zipSource: Source,
        urlPrefix: String,
        retain: Boolean = true,
        static: Boolean = true,
    )

    /**
     * Retrieve if cached.
     *
     * Expect-SHA-256 is set, then we can search for the given sha-256.
     */
    fun retrieve(
        request: HttpRequest,
    ): HttpResponse?


    /**
     * Run a bulk query to check if the given urls are available in the cache.
     *
     * @param urls a set of URLs to check to see if they are available in the cache
     * @return A map of the which URLs are cached (url to boolean)
     */
    fun hasEntries(
        urls: Set<String>
    ): Map<String, Boolean>


    /**
     * Create retention locks.
     */
    fun addRetentionLocks(locks: List<EntryLockRequest>): List<Pair<EntryLockRequest, RetentionLock>>

    fun removeRetentionLocks(lockIds: List<Int>)

    fun close()

    companion object {

        const val HEADER_FIRST_STORED_TIMESTAMP = "UCache-First-Stored"

        const val HEADER_LAST_VALIDATED_TIMESTAMP = "UCache-Last-Validated"

    }


}