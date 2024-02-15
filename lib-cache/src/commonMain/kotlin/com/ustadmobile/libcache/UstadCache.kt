package com.ustadmobile.libcache

import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.response.HttpResponse

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
     * Filter that will be used by the cache to determine if a given entry should be stored using
     * compression. This should generally be true for text types e.g. css, javascript, html, json, etc.
     * Should not be used for types that are already compressed e.g. images, audio, video, zips, etc.
     *
     * When it is determined that an entry should be compressed, then it will be stored on disk as a
     * compressed file. When it is served, the content-encoding header will be used (which is stored
     * together with all other headers when added to the cache).
     */
    val storageCompressionFilter: CacheStorageCompressionFilter

    /**
     * Store a set of requests with their corresponding response.
     */
    fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener? = null,
    ): List<StoreResult>

    /**
     * Update the last validated information for a given set of urls. This should be performed when
     * another component (e.g. the OkHttp interceptor) has performed a successful validation e.g.
     * received a Not-Modified response from the origin server.
     *
     * The not-modified response from the origin server will likely not have all the original
     * headers (e.g. content-length, content-type, etc). This is valid. The not-modified
     * response can likely contain validation / cache related headers like Age, Last-Modified,
     * etc. Generally, any headers from the validation response will override the previous
     * headers.
     *
     * An exception is content-length: some servers e.g. KTOR (incorrectly) specify a
     * content-length of zero on 304 responses. This is not valid. The content-length
     * header will be filtered out. By definition: 304 means NOT MODIFIED, and if it was
     * not modified, the content-length should NOT have changed.
     *
     * The headers stored in the cache will be updated from the validated entry, with any invalid
     * headers (as outlined above) filtered out.
     */
    fun updateLastValidated(validatedEntry: ValidatedEntry)

    /**
     * Retrieve if cached.
     *
     * Expect-SHA-256 is set, then we can search for the given sha-256.
     */
    fun retrieve(
        request: HttpRequest,
    ): HttpResponse?

    fun getCacheEntry(url: String): CacheEntry?


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

        const val DEFAULT_SIZE_LIMIT = (100 * 1024 * 1024).toLong()

    }


}