package com.ustadmobile.libcache

import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libcache.UstadCache.Companion.HEADER_LAST_VALIDATED_TIMESTAMP
import com.ustadmobile.libcache.base64.encodeBase64
import com.ustadmobile.libcache.cachecontrol.ResponseValidityChecker
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.db.entities.RequestedEntry
import com.ustadmobile.libcache.headers.CouponHeader.Companion.COUPON_ACTUAL_SHA_256
import com.ustadmobile.libcache.headers.CouponHeader.Companion.COUPON_STATIC
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.MimeTypeHelper
import com.ustadmobile.libcache.headers.asString
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.io.sha256
import com.ustadmobile.libcache.io.transferToAndGetSha256
import com.ustadmobile.libcache.io.unzipTo
import com.ustadmobile.libcache.logging.UstadCacheLogger
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.CacheResponse
import com.ustadmobile.libcache.response.HttpPathResponse
import com.ustadmobile.libcache.response.HttpResponse
import com.ustadmobile.libcache.uuid.randomUuid
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.atomicfu.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 *
 * @param sizeLimit A function that returns the current size limit for the cache. This will be
 *        invoked on the periodic trims that are run. The limit applies to evictable entries e.g.
 *        entries which do not have any retentionlock.
 * @param lastAccessedCommitInterval the interval period to commit (in batches) updates to entry
 *        last accessed times.
 */
class UstadCacheImpl(
    private val fileSystem: FileSystem = SystemFileSystem,
    cacheName: String = "",
    storagePath: Path,
    private val db: UstadCacheDb,
    internal val mimeTypeHelper: MimeTypeHelper,
    sizeLimit: () -> Long = { UstadCache.DEFAULT_SIZE_LIMIT },
    private val logger: UstadCacheLogger? = null,
    private val listener: UstadCache.CacheListener? = null,
    private val lastAccessedCommitInterval: Int = 5_000,
    private val trimInterval: Int = 30_000,
    private val responseValidityChecker: ResponseValidityChecker = ResponseValidityChecker(),
    private val trimmer: UstadCacheTrimmer = UstadCacheTrimmer(
        db = db,
        fileSystem = fileSystem,
        logger = logger,
        sizeLimit = sizeLimit,
    )
) : UstadCache {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val tmpDir = Path(storagePath, "tmp")

    private val dataDir = Path(storagePath, "data")

    private val tmpCounter = atomic(0)

    private val batchIdAtomic = atomic(0)

    private val logPrefix = "UstadCache($cacheName):"

    private val pendingLastAccessedUpdates = atomic(emptyList<LastAccessedUpdate>())

    private val pendingLockRemovals = atomic(emptyList<Int>())

    data class CacheEntryAndTmpFile(
        val cacheEntry: CacheEntry,
        val entryToStore: CacheEntryToStore,
        val tmpFile: Path,
    )

    data class LastAccessedUpdate(
        val key: String,
        val accessTime: Long,
    )


    init {
        scope.launch {
            while(isActive) {
                delay(lastAccessedCommitInterval.toLong())
                commitLastAccessedUpdates()
            }
        }

        scope.launch {
            while(isActive) {
                delay(trimInterval.toLong())
                trimmer.trim()
            }
        }
    }

    private fun insertRetentionLocks(
        lockRequests: List<EntryLockRequest>,
        md5Digest: Md5Digest,
    ): List<Pair<EntryLockRequest, RetentionLock>> {
        return lockRequests.map {
            val lock = RetentionLock(
                lockKey = md5Digest.urlKey(it.url),
            )
            val lockId = db.retentionLockDao.insert(lock).toInt()
            it to lock.copy(lockId = lockId)
        }
    }

    override fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener?
    ): List<StoreResult> {
        val md5Digest = Md5Digest()
        try {
            logger?.d(LOG_TAG) { "$logPrefix storerequest ${storeRequest.size} entries" }
            fileSystem.takeIf { !fileSystem.exists(dataDir) }?.createDirectories(dataDir)
            fileSystem.takeIf { !fileSystem.exists(tmpDir) }?.createDirectories(tmpDir)

            /**
             * Go through everything that is requested to be stored: create a list of CacheEntryAndTmpFile
             * which contains the CacheEntry entity (headers, request status, etc) and a tmp
             * file containing the request body. Run checksums as needed to get a SHA256 checksum
             * for every response body.
             */
            val storeRequestEntriesAndTmpFiles = storeRequest.map { entryToStore ->
                val response = entryToStore.response
                val tmpFile = Path(tmpDir, "${tmpCounter.incrementAndGet()}.tmp")
                val url = entryToStore.request.url

                val sha256FromTransfer = if(entryToStore.responseBodyTmpLocalPath != null) {
                    //If the entry to store is in a temporary path where it is acceptable to just
                    //move the file into the cache, then we will move (instead of copying) the file
                    fileSystem.atomicMove(entryToStore.responseBodyTmpLocalPath, tmpFile)
                    null
                }else {
                    val bodySource = response.bodyAsSource()

                    if(bodySource == null) {
                        val e = IllegalArgumentException("Response for $url has " +
                                "no body. That should not have been stored in cache. Something badly wrong.")
                        logger?.e(LOG_TAG, "$logPrefix BodySource for ${entryToStore.request.url} is null", e)
                        throw e
                    }

                    bodySource.transferToAndGetSha256(tmpFile).sha256.encodeBase64()
                }
                val sha256FromHeader = if(entryToStore.skipChecksumIfProvided)
                    response.headers[COUPON_ACTUAL_SHA_256]
                else
                    null

                val sha256 = sha256FromTransfer ?: sha256FromHeader
                    ?: fileSystem.source(tmpFile).buffered().sha256().encodeBase64()

                val headersStr = headersBuilder {
                    takeFrom(response.headers)
                    header(COUPON_ACTUAL_SHA_256, sha256)
                }.asString()

                logger?.v(LOG_TAG, "$logPrefix copied request data for $url to $tmpFile (sha256=$sha256)")

                val cacheFlags = if(response.headers[COUPON_STATIC]?.toBooleanStrictOrNull() == true) {
                    CacheEntry.CACHE_FLAG_STATIC
                }else {
                    0
                }

                CacheEntryAndTmpFile(
                    cacheEntry = CacheEntry(
                        key = md5Digest.urlKey(entryToStore.request.url),
                        url = entryToStore.request.url,
                        responseBodySha256 = sha256,
                        statusCode = entryToStore.response.responseCode,
                        responseHeaders = headersStr,
                        cacheFlags = cacheFlags,
                        lastValidated = systemTimeInMillis(),
                    ),
                    entryToStore = entryToStore,
                    tmpFile = tmpFile
                )
            }

            logger?.v(LOG_TAG) { "$logPrefix cacheEntries created ${storeRequestEntriesAndTmpFiles.size} entries" }
            val batchId = batchIdAtomic.incrementAndGet()

            /*
             * Find what entries are already stored in the database. Update entries that are
             * already present, insert new entries.
             */
            val (tmpFilesToDelete, locksCreated) = db.withDoorTransaction { _ ->
                db.requestedEntryDao.insertList(storeRequestEntriesAndTmpFiles.map {
                    RequestedEntry(
                        requestSha256 = it.cacheEntry.responseBodySha256 ?: "",
                        requestedKey = md5Digest.urlKey(it.cacheEntry.url),
                        batchId = batchId,
                    )
                })

                val entriesInCache = db.cacheEntryDao.findByRequestBatchId(batchId)
                val entriesInCacheMap = entriesInCache.associateBy { it.key }
                val tmpFilesToDelete = mutableListOf<Path>()

                val entriesToSave = storeRequestEntriesAndTmpFiles.mapNotNull { storeRequest ->
                    val storedEntry = entriesInCacheMap[storeRequest.cacheEntry.key]
                    val storedEntryHeaders = storedEntry?.responseHeaders?.let {
                        HttpHeaders.fromString(it)
                    }

                    val etagOrLastModifiedMatches = if(storedEntryHeaders != null) {
                        responseValidityChecker.isMatchingEtagOrLastModified(
                            storedEntryHeaders, storeRequest.entryToStore.response.headers
                        )
                    }else {
                        false
                    }

                    if(storedEntry != null && etagOrLastModifiedMatches) {
                        //If the entry is already saved and still valid, there is nothing to do
                        tmpFilesToDelete += storeRequest.tmpFile
                        null
                    }else {
                        val destPath = Path(dataDir, randomUuid())
                        fileSystem.atomicMove(storeRequest.tmpFile, destPath)

                        //Where there is a stored entry that is invalid, file should be deleted
                        storedEntry?.storageUri?.also { oldStorageUri ->
                            fileSystem.delete(Path(oldStorageUri))
                        }

                        storeRequest.cacheEntry.copy(
                            storageUri = destPath.toString(),
                            storageSize = fileSystem.metadataOrNull(destPath)?.size ?: 0,
                        )
                    }
                }

                db.cacheEntryDao.upsertList(entriesToSave)
                db.requestedEntryDao.deleteBatch(batchId)

                val locks = insertRetentionLocks(
                    lockRequests = storeRequest.filter { it.createRetentionLock }.map {
                        EntryLockRequest(it.request.url)
                    },
                    md5Digest = md5Digest,
                ).associate {
                    it.first.url to it.second
                }

                //End transaction block by providing a list of those tmp files that we don't need
                tmpFilesToDelete to locks
            }

            logger?.d(LOG_TAG, "$logPrefix deleting ${tmpFilesToDelete.size} tmp files")
            tmpFilesToDelete.forEach {
                fileSystem.delete(it)
            }

            logger?.d(LOG_TAG, "$logPrefix db transaction completed")
            listener?.onEntriesStored(storeRequest)

            return storeRequest.map {
                StoreResult(
                    urlKey = md5Digest.urlKey(it.request.url),
                    request = it.request,
                    response = it.response,
                    lockId = locksCreated[it.request.url]?.lockId ?: 0
                )
            }
        }catch(e: Throwable) {
            throw IllegalStateException("Could not cache", e)
        }
    }

    override fun storeZip(
        zipSource: Source,
        urlPrefix: String,
        retain: Boolean,
        static: Boolean,
    ) {
        if(!urlPrefix.endsWith("/"))
            throw IllegalArgumentException("Url prefix must end with / !")

        fileSystem.takeIf { !it.exists(tmpDir) }?.createDirectories(tmpDir)
        val unzippedEntries = zipSource.unzipTo(tmpDir)

        val entriesToCache = unzippedEntries.map {
            val request = requestBuilder {
                url = "$urlPrefix${it.name}"
            }

            CacheEntryToStore(
                request = request,
                response = HttpPathResponse(
                    path = it.path,
                    fileSystem = fileSystem,
                    mimeType = mimeTypeHelper.guessByExtension(it.name.substringAfterLast("."))
                        ?: "application/octet-stream",
                    request = request,
                    extraHeaders = headersBuilder {
                        header(COUPON_ACTUAL_SHA_256, it.sha256.encodeBase64())
                        if(static)
                            header(COUPON_STATIC, "true")
                    }
                ),
                skipChecksumIfProvided = true,
            )
        }

        store(entriesToCache)
    }

    /**
     * Retrieve a response from the cache, if available. The response might be stale.
     *
     * NOTE: If we know in advance that a particular batch is going to be requested, we can run
     * a statusCheckCache to avoid running 100s-1000+ SQL queries for tiny jsons etc.
     *
     */
    override fun retrieve(request: HttpRequest): HttpResponse? {
        logger?.i(LOG_TAG, "$logPrefix Retrieve ${request.url}")

        val key = Md5Digest().digest(request.url.encodeToByteArray()).encodeBase64()
        val entry = db.cacheEntryDao.findEntryAndBodyByKey(key)
        if(entry != null) {
            logger?.d(LOG_TAG, "$logPrefix FOUND ${request.url}")
            pendingLastAccessedUpdates.update { prev ->
                prev + LastAccessedUpdate(key, systemTimeInMillis())
            }

            return CacheResponse(
                fileSystem = fileSystem,
                request = request,
                headers = headersBuilder {
                    takeFrom(HttpHeaders.fromString(entry.responseHeaders))
                    header(HEADER_LAST_VALIDATED_TIMESTAMP, entry.lastValidated.toString())
                },
                storageUri = entry.storageUri,
                httpResponseCode = entry.statusCode
            )
        }

        logger?.d(LOG_TAG, "$logPrefix MISS ${request.url}")
        return null
    }


    override fun hasEntries(urls: Set<String>): Map<String, Boolean> {
        val batchId = batchIdAtomic.incrementAndGet()
        val md5Digest = Md5Digest()
        val keysNotPresent = db.withDoorTransaction {
            db.requestedEntryDao.insertList(
                urls.map {  url ->
                    RequestedEntry(
                        batchId = batchId,
                        requestedKey =  md5Digest.urlKey(url)
                    )
                }
            )
            db.requestedEntryDao.findKeysNotPresent(batchId).also {
                db.requestedEntryDao.deleteBatch(batchId)
            }
        }

        return urls.associateWith { url ->
            (md5Digest.urlKey(url) !in keysNotPresent)
        }
    }

    override fun addRetentionLocks(locks: List<EntryLockRequest>): List<Pair<EntryLockRequest, RetentionLock>> {
        val md5Digest = Md5Digest()
        return db.withDoorTransaction {
            insertRetentionLocks(locks, md5Digest)
        }
    }

    /**
     * Lock removal is done by adding it to the pending list. This isn't urgent. This avoids a large
     * number of database transactions running when lots of small files are being uploaded
     */
    override fun removeRetentionLocks(lockIds: List<Int>) {
        pendingLockRemovals.update { prev ->
            prev + lockIds
        }
    }

    private fun commitLastAccessedUpdates() {
        val updatesPending = pendingLastAccessedUpdates.getAndUpdate {
            emptyList()
        }

        val lockRemovalsPending = pendingLockRemovals.getAndUpdate {
            emptyList()
        }

        if(updatesPending.isEmpty() && lockRemovalsPending.isEmpty())
            return

        val updatesMap = mutableMapOf<String, Long>()
        updatesPending.forEach {
            updatesMap[it.key] = it.accessTime
        }

        db.withDoorTransaction {
            updatesMap.forEach {
                db.cacheEntryDao.updateLastAccessedTime(it.key, it.value)
            }

            db.retentionLockDao.delete(lockRemovalsPending.map { RetentionLock(lockId = it) } )
        }
    }

    override fun close() {
        scope.cancel()
        commitLastAccessedUpdates()
    }

    companion object {
        const val LOG_TAG = "UstadCache"
    }
}