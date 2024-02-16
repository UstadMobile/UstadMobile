package com.ustadmobile.libcache

import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.door.util.NullOutputStream
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libcache.UstadCache.Companion.HEADER_LAST_VALIDATED_TIMESTAMP
import com.ustadmobile.libcache.cachecontrol.ResponseValidityChecker
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.db.entities.RequestedEntry
import com.ustadmobile.libcache.headers.MergedHeaders
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.asString
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.headers.integrity
import com.ustadmobile.libcache.headers.mapHeaders
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.requireMetadata
import com.ustadmobile.libcache.io.useAndReadSha256
import com.ustadmobile.libcache.io.transferToAndGetSha256
import com.ustadmobile.libcache.io.uncompress
import com.ustadmobile.libcache.logging.UstadCacheLogger
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.response.CacheResponse
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
import kotlinx.io.asSink
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
    private val pathsProvider: CachePathsProvider,
    private val db: UstadCacheDb,
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
    ),
    override val storageCompressionFilter: CacheStorageCompressionFilter = DefaultCacheCompressionFilter(),
) : UstadCache {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val tmpCounter = atomic(0)

    private val batchIdAtomic = atomic(0)

    private val logPrefix = "UstadCache($cacheName):"

    private val pendingLastAccessedUpdates = atomic(emptyList<LastAccessedUpdate>())

    private val pendingLockRemovals = atomic(emptyList<Int>())

    /**
     * Data class that is used to track the status of a CacheEntryToStore as it is processed.
     *
     * @param cacheEntry the CacheEntry entity as it will be saved into the database
     * @param entryToStore the entryToStore request that provided as an argument to the store function
     * @param tmpFile the temporary file where data is being kept
     * @param responseHeaders the response headers (canonical) as they will be stored including the
     *        etag integrity values etc. that get added by default
     * @param tmpFileNeedsDeleted if true, then the tmpFile must be deleted before completing
     *        the store function. Can be false when responseBodyTmpLocalPath is provided, because
     *        then the responseBodyTmpLocalPath will be moved (not copied).
     * @param previousStorageUriToDelete if it is determined that new data will be replacing old
     *        data, then the previous body data will be deleted.
     *
     */
    private data class CacheEntryInProgress(
        val cacheEntry: CacheEntry,
        val entryToStore: CacheEntryToStore,
        val tmpFile: Path,
        val responseHeaders: HttpHeaders,
        val tmpFileNeedsDeleted: Boolean = false,
        val lockId: Int = 0,
        val previousStorageUriToDelete: String? = null,
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
        val timeNow = systemTimeInMillis()
        try {
            logger?.d(LOG_TAG) { "$logPrefix storerequest ${storeRequest.size} entries" }

            /**
             * Go through everything that is requested to be stored: create a list of CacheEntryInProgress
             * which contains the CacheEntry entity (headers, request status, etc) and a tmp
             * file containing the request body. Run checksums as needed to get a SHA256 checksum
             * for every response body.
             */
            val entriesWithTmpFileAndIntegrityInfo = storeRequest.map { entryToStore ->
                val response = entryToStore.response
                val entryPaths = pathsProvider(entryToStore)
                fileSystem.createDirectories(entryPaths.tmpWorkPath)
                val tmpFile = Path(entryPaths.tmpWorkPath,
                    "${tmpCounter.incrementAndGet()}.tmp")
                val url = entryToStore.request.url
                val storeCompressionType = storageCompressionFilter.invoke(
                    url = entryToStore.request.url,
                    requestHeaders = entryToStore.request.headers,
                    responseHeaders = entryToStore.response.headers
                )
                val responseCompression = CompressionType.byHeaderVal(
                    entryToStore.response.headers["content-encoding"]
                )
                val overrideHeaders = mutableMapOf<String, List<String>>()

                @Suppress("ArrayInDataClass")
                data class Sha256AndInflateSize(val sha256: ByteArray?, val inflatedSize: Long)

                val (sha256IntegrityFromTransfer, uncompressedSize) = if(
                    entryToStore.responseBodyTmpLocalPath != null && storeCompressionType == responseCompression
                ) {
                    //If the entry to store is in a temporary path where it is acceptable to just
                    //move the file into the cache, and it is already compressed with the desired
                    // compression type for storage, then we will move (instead of copying) the file
                    fileSystem.atomicMove(entryToStore.responseBodyTmpLocalPath, tmpFile)
                    val inflatedSize = if(storeCompressionType == CompressionType.NONE) {
                        fileSystem.requireMetadata(tmpFile).size
                    }else {
                        //"transfer" to a nulloutputstream sink to count uncompressed size
                        fileSystem.source(tmpFile).buffered()
                            .uncompress(storeCompressionType).transferTo(NullOutputStream().asSink())
                    }

                    Sha256AndInflateSize(null, inflatedSize)
                }else {
                    val bodySource = response.bodyAsSource()

                    if(bodySource == null) {
                        val e = IllegalArgumentException("Response for $url has " +
                                "no body. That should not have been stored in cache. Something badly wrong.")
                        logger?.e(LOG_TAG, "$logPrefix BodySource for ${entryToStore.request.url} is null", e)
                        throw e
                    }

                    val transferResult = bodySource.transferToAndGetSha256(tmpFile,
                        responseCompression, storeCompressionType)
                    overrideHeaders["content-encoding"] = listOf(storeCompressionType.headerVal)
                    overrideHeaders["content-length"] = listOf(fileSystem.requireMetadata(tmpFile).size.toString())

                    Sha256AndInflateSize(transferResult.sha256, transferResult.transferred)
                }

                val integrityFromHeaders = if(entryToStore.skipChecksumIfProvided)
                    response.headers.integrity()
                else
                    null

                val integrity = sha256IntegrityFromTransfer?.let { sha256Integrity(it) }
                    ?: integrityFromHeaders
                    ?: sha256Integrity(fileSystem.source(tmpFile).buffered().useAndReadSha256())

                val effectiveHeaders = if(overrideHeaders.isNotEmpty()) {
                    MergedHeaders(HttpHeaders.fromMap(overrideHeaders), response.headers)
                }else {
                    response.headers
                }

                logger?.v(LOG_TAG, "$logPrefix copied request data for $url to $tmpFile (integrity=$integrity)")

                CacheEntryInProgress(
                    cacheEntry = CacheEntry(
                        key = md5Digest.urlKey(entryToStore.request.url),
                        url = entryToStore.request.url,
                        integrity = integrity,
                        statusCode = entryToStore.response.responseCode,
                        responseHeaders = effectiveHeaders.asString(),
                        lastValidated = timeNow,
                        lastAccessed = timeNow,
                        uncompressedSize = uncompressedSize
                    ),
                    entryToStore = entryToStore,
                    tmpFile = tmpFile,
                    responseHeaders = entryToStore.response.headers,
                )
            }

            logger?.v(LOG_TAG) { "$logPrefix cacheEntries created ${entriesWithTmpFileAndIntegrityInfo.size} entries" }
            val batchId = batchIdAtomic.incrementAndGet()

            /*
             * Find what entries are already stored in the database. Update entries that are
             * already present, insert new entries.
             */
            val dbProcessedEntries = db.withDoorTransaction { _ ->
                db.requestedEntryDao.insertList(entriesWithTmpFileAndIntegrityInfo.map {
                    RequestedEntry(
                        requestSha256 = it.cacheEntry.integrity ?: "",
                        requestedKey = md5Digest.urlKey(it.cacheEntry.url),
                        batchId = batchId,
                    )
                })

                val entriesInCache = db.cacheEntryDao.findByRequestBatchId(batchId)
                val entriesWithLock = db.cacheEntryDao.findEntriesWithLock(batchId).toSet()
                val entriesInCacheMap = entriesInCache.associateBy {
                    it.key
                }

                val entriesToSave = entriesWithTmpFileAndIntegrityInfo.map { entryInProgress ->
                    val storedEntry = entriesInCacheMap[entryInProgress.cacheEntry.key]
                    val storedEntryHeaders = storedEntry?.responseHeaders?.let {
                        HttpHeaders.fromString(it)
                    }

                    val etagOrLastModifiedMatches = if(storedEntryHeaders != null) {
                        responseValidityChecker.isMatchingEtagOrLastModified(
                            storedEntryHeaders, entryInProgress.entryToStore.response.headers
                        )
                    }else {
                        false
                    }

                    if(storedEntry != null && etagOrLastModifiedMatches && storedEntryHeaders != null) {
                        /* If the entry is already saved and still valid. We will not store the body,
                         * but we will upsert the CacheEntry entity so that the last validated and
                         * last accessed times are updated.
                         *
                         * Because the body data will not be modified, the content-length and
                         * content-encoding MUST NOT be changed.
                         */
                        val overrideHeaders = buildMap {
                            NOT_MODIFIED_IGNORE_HEADERS.forEach { headerName ->
                                storedEntryHeaders[headerName]?.also { storedEntryHeaderVal ->
                                    put(headerName, listOf(storedEntryHeaderVal))
                                }
                            }
                        }

                        entryInProgress.copy(
                            tmpFileNeedsDeleted = true,
                            cacheEntry = entryInProgress.cacheEntry.copy(
                                storageUri = storedEntry.storageUri,
                                storageSize = storedEntry.storageSize,
                                responseHeaders = MergedHeaders(
                                    HttpHeaders.fromMap(overrideHeaders),
                                    entryInProgress.responseHeaders,
                                    storedEntryHeaders,
                                ).asString()
                            )
                        )
                    }else {
                        //The new entry does not validate, so we will need to store the new body.
                        val destPaths = pathsProvider(entryInProgress.entryToStore)
                        val destPathParent = if(
                            entryInProgress.cacheEntry.key in entriesWithLock ||
                            entryInProgress.entryToStore.createRetentionLock
                        ) {
                            destPaths.persistentPath
                        }else {
                            destPaths.cachePath
                        }
                        fileSystem.createDirectories(destPathParent)
                        val destPath = Path(destPathParent.toString(), randomUuid())
                        fileSystem.atomicMove(entryInProgress.tmpFile, destPath)

                        entryInProgress.copy(
                            cacheEntry = entryInProgress.cacheEntry.copy(
                                storageUri = destPath.toString(),
                                storageSize = fileSystem.metadataOrNull(destPath)?.size ?: 0,
                            ),
                            tmpFileNeedsDeleted = false,
                            //Where there is a stored entry that is invalid, file should be deleted
                            previousStorageUriToDelete = storedEntry?.storageUri,
                        )
                    }
                }

                db.cacheEntryDao.upsertList(entriesToSave.map { it.cacheEntry } )
                db.requestedEntryDao.deleteBatch(batchId)

                val locks = insertRetentionLocks(
                    lockRequests = storeRequest.filter { it.createRetentionLock }.map {
                        EntryLockRequest(it.request.url)
                    },
                    md5Digest = md5Digest,
                ).associate {
                    it.second.lockKey to it.second.lockId
                }

                //ideally - map by key not url
                entriesToSave.map { entry ->
                    entry.copy(
                        lockId = locks[entry.cacheEntry.key] ?: 0
                    )
                }
            }

            val tmpFilesToDelete = dbProcessedEntries.filter {
                it.tmpFileNeedsDeleted
            }.map {
                it.tmpFile
            }

            val oldVersionBodiesToDelete = dbProcessedEntries.mapNotNull { entry ->
                entry.previousStorageUriToDelete?.let { Path(it) }
            }

            logger?.d(LOG_TAG, "$logPrefix deleting ${tmpFilesToDelete.size} tmp files")
            (tmpFilesToDelete + oldVersionBodiesToDelete).forEach {
                fileSystem.delete(it)
            }

            logger?.d(LOG_TAG, "$logPrefix db transaction completed")
            listener?.onEntriesStored(storeRequest)

            return dbProcessedEntries.map {
                StoreResult(
                    urlKey = it.cacheEntry.key,
                    request = it.entryToStore.request,
                    response = it.entryToStore.response,
                    integrity = it.cacheEntry.integrity!!,
                    storageSize = it.cacheEntry.storageSize,
                    lockId = it.lockId
                )
            }
        }catch(e: Throwable) {
            throw IllegalStateException("Could not cache", e)
        }
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

        val key = Md5Digest().urlKey(request.url)
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
                httpResponseCode = entry.statusCode,
                uncompressedSize = entry.uncompressedSize,
            )
        }

        logger?.d(LOG_TAG, "$logPrefix MISS ${request.url}")
        return null
    }

    override fun updateLastValidated(validatedEntry: ValidatedEntry) {
        val md5 = Md5Digest()
        val timeNow = systemTimeInMillis()
        val urlKey = md5.urlKey(validatedEntry.url)

        db.withDoorTransaction {
            val existingEntry = db.cacheEntryDao.findEntryAndBodyByKey(
                key = urlKey,
            ) ?: return@withDoorTransaction

            val existingHeaders = HttpHeaders.fromString(existingEntry.responseHeaders)

            val newHeadersCorrected = validatedEntry.headers.mapHeaders { headerName, headerValue ->
                when {
                    NOT_MODIFIED_IGNORE_HEADERS.any { headerName.equals(it, true) } -> null
                    else -> headerValue
                }
            }
            val newHeaders = MergedHeaders(newHeadersCorrected, existingHeaders)

            db.cacheEntryDao.updateValidation(
                key = urlKey,
                headers = newHeaders.asString(),
                lastValidated = timeNow,
                lastAccessed = timeNow,
            )
        }
    }

    override fun getCacheEntry(url: String): CacheEntry? {
        return db.cacheEntryDao.findEntryAndBodyByKey(Md5Digest().urlKey(url))
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

        /**
         * When an entry is validated, most headers will be updated with those found on the 304
         * not modified response e.g. Age, Cache-Control, Last-Modified etc.
         *
         * All values on the 304 not-modified SHOULD be the same as would otherwise be returned, however:
         *  1) KTOR, and other servers, use content-length: 0 (a 304 not-modified response has no
         *     body, so that response has a length of zero, but strictly speaking, this is wrong)
         *  2) content-encoding : this could be changed internally (e.g. by updating what mime types
         *     are or are not compressed). When a 304 response is received, the response body stored
         *     on disk is not changed, so the content-encoding must NEVER change.
         */
        private val NOT_MODIFIED_IGNORE_HEADERS = listOf("content-length", "content-encoding")

    }
}