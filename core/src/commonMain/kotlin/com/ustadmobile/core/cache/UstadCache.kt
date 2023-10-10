package com.ustadmobile.core.cache

data class CacheRetentionLock(
    val lockId: Int,
    val timeCreated: Long,
    val url: String,
)

data class CacheRetentionJoin(
    val lockId: Int,
    val tableId: Int,
    val entityId: Long,
    val url: String,
    val status: Int, //Active, inactive, upload_pending
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
     *
     *
     * @param loadFromUri - if set, the data will be loaded from the given uri (e.g. can be used to
     *       prime the cache from local data).
     */
    suspend fun cache(
        url: String,
        loadFromUri: String? = null,
        requestHeaders: Map<String, String>,
        responseHeaders: Map<String, String>?,
        lock: Boolean,
        progressListener: () -> Unit,
    ) //Return cache result that includes the SHA256

    /**
     * Only on JVM for use with OKHttp
     *
     * Expect-SHA-256 is set, then we can search for the given sha-256.
     */
    suspend fun retrieve(
        request: String,//Actual request object
    ): String //response

    suspend fun addRetentionLocks(locks: List<CacheRetentionLock>): List<Int>

    suspend fun removeRetentionLocks(lockIds: List<Int>)

    /**
     * Ustad-specific logic
     *
     *  Flow when user uploads a picture from web:
     *
     *  1) ViewModel(JS) calls storeBlob when saving. Makes post request. Server endpoint will
     *     a) run the sha256 sum on it,
     *     b) cache it as: http(s)://endpoint.com/api/blob/tableId/entityUid - avoids urls being changed
     *          for the same entity. Endpoint will check permission and last-modified time
     *     c) insert (if needed) CacheRetentionJoin.
     *
     *  2) ViewModel(JS) will set the URL on the entity so it can be used (e.g. as picture etc)
     *
     *  3) Trigger on server database (on entity update, if new.pictureUrl != old.pictureUrl, then
     *     then update CacheRetentionJoin SET status = inactive WHERE tableId = tableId, entityId = entityId,
     *     url = null. Periodically collect inactive CacheRetentionJoins so that we can
     *     then delete the lock.
     *
     *  Flow when user uploads a picture on Android/Desktop:
     *
     *  1) ViewModel calls storeBlob using Android / File URI. This checks the SHA-256 sum. Then calls
     *     cache (with loadFromUri set to the local uri), and adds a retentionlock. Inserts
     *     CacheRetentionJoin locally, sets the status to pending_upload. Submits a workrequest.
     *  2) ViewModel will set the URL on the entity so it can be used (e.g. as picture etc).
     *  3) WorkRequest makes http post request (as per JS). After completion of the upload,
     *     sets the retention lock status to inactive.
     */
    suspend fun storeBlob(
        localDataUri: String,//Should be List<BlobStoreRequest>
        endpointUrl: String,
        retentionJoin: CacheRetentionJoin,
    ): String //StoreResult: include url, retention lock id if requested



}