package com.ustadmobile.libcache

import com.ustadmobile.door.ext.concurrentSafeMapOf
import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.door.util.NullOutputStream
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.MergedHeaders
import com.ustadmobile.ihttp.headers.asString
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.ihttp.headers.mapHeaders
import com.ustadmobile.libcache.UstadCache.Companion.HEADER_LAST_VALIDATED_TIMESTAMP
import com.ustadmobile.libcache.cachecontrol.ResponseValidityChecker
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.CacheEntryAndLocks
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.db.entities.RequestedEntry
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.moveWithFallback
import com.ustadmobile.libcache.io.requireMetadata
import com.ustadmobile.libcache.io.useAndReadSha256
import com.ustadmobile.libcache.io.transferToAndGetSha256
import com.ustadmobile.libcache.io.uncompress
import com.ustadmobile.libcache.logging.UstadCacheLogger
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.libcache.response.CacheResponse
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.libcache.headers.integrity
import com.ustadmobile.libcache.util.LruMap
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
import kotlin.concurrent.withLock
/**
 *
 * @param sizeLimit A function that returns the current size limit for the cache. This will be
 *        invoked on the periodic trims that are run. The limit applies to evictable entries e.g.
 *        entries which do not have any retentionlock.
 * @param databaseCommitInterval the interval period to commit updates to the database. When entries
 */
class UstadCacheImpl(
    private val fileSystem: FileSystem = SystemFileSystem,
    cacheName: String = "",
    private val pathsProvider: CachePathsProvider,
    private val db: UstadCacheDb,
    sizeLimit: () -> Long = { UstadCache.DEFAULT_SIZE_LIMIT },
    private val logger: UstadCacheLogger? = null,
    private val listener: UstadCache.CacheListener? = null,
    private val databaseCommitInterval: Int = 2_000,
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

    private val lockIdAtomic = atomic(systemTimeInMillis())

    private val logPrefix = "UstadCache($cacheName):"

    private val pendingLastAccessedUpdates = atomic(emptyList<LastAccessedUpdate>())

    private val pendingLockRemovals = atomic(emptyList<Long>())

    private val pendingLockUpserts = atomic(emptyList<RetentionLock>())

    private val pendingCacheEntryUpdates = atomic(emptyList<CacheEntry>())

    private val pendingCacheEntryDeletes = atomic(emptyList<CacheEntry>())

    /**
     * The LruMap is the in-memory cache of entries. It does not include the actual response data.
     * This can reduce both the number of database queries and significantly reduce the number of
     * database transactions that need to be performed.
     *
     * When modifications happen (when an item is stored, updated, removed, etc) the updates are done
     * in the LRU map and any pending database changes are added to the relevant pending atomic list.
     * Database updates are then performed in batches, significantly improving performance. This is
     * especially helpful when many small items are being loaded.
     */
    private val lruMap = LruMap<String, CacheEntryAndLocks>(concurrentSafeMapOf())

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
        val responseHeaders: IHttpHeaders,
        val tmpFileNeedsDeleted: Boolean = false,
        val lockId: Long = 0,
        val previousStorageUriToDelete: String? = null,
    )

    data class LastAccessedUpdate(
        val key: String,
        val accessTime: Long,
    )


    init {
        scope.launch {
            while(isActive) {
                delay(databaseCommitInterval.toLong())
                commit()
            }
        }

        scope.launch {
            while(isActive) {
                delay(trimInterval.toLong())
                commit()
                trimmer.trim()
            }
        }

        scope.launch {
            trimmer.evictedEntriesFlow.collect { evictedEntries ->
                evictedEntries.forEach { evictedKey ->
                    lruMap.computeIfPresent(evictedKey) { _, entry ->
                        entry.copy(
                            entry = null
                        )
                    }
                }
            }
        }
    }

    /**
     * @param entries The entries loaded (including CacheEntry entity and any associated locks
     * @param pending if loadEntries was called with loadFromDb=false, then this is a list of the
     *        entries that were not loaded because the status is not in the Lru in memory cache
     * @param loadedFromDb true if anything was loaded from db, false otherwise.
     */
    private data class LoadEntriesResult(
        val entries: List<CacheEntryAndLocks>,
        val pending: List<RequestedEntry>,
        val loadedFromDb: Boolean,
    )

    /*
     * Load entries from the LruCache. When loadFromDb is enabled, then any entries not found in the
     * in memory lru cache will be loaded from the database. When loadFromDb is not enabled, entries
     * not found in the cache will be returned in the pending list of the LoadEntriesResult
     */
    private fun loadEntries(
        requestEntries: List<RequestedEntry>,
        loadFromDb: Boolean = true,
    ): LoadEntriesResult {
        val (entriesInLru, entriesNotInLru) = requestEntries.partition {
            lruMap.containsKey(it.requestedKey)
        }

        val entriesFromLruList = entriesInLru.mapNotNull {
            lruMap[it.requestedKey]
        }

        if(!loadFromDb || entriesNotInLru.isEmpty()) {
            return LoadEntriesResult(entriesFromLruList, entriesNotInLru, loadedFromDb = false)
        }

        return db.withDoorTransaction {
            val batchId = batchIdAtomic.incrementAndGet()
            val entriesFromLruMap = entriesFromLruList.associateBy { it.urlKey }

            val entriesToQueryDb = requestEntries.filter {
                !entriesFromLruMap.containsKey(it.requestedKey)
            }

            db.requestedEntryDao.insertList(entriesToQueryDb)

            val entriesInDb = db.cacheEntryDao.findByRequestBatchId(batchId)
                .associateBy { it.key }
            val locksInDb = db.retentionLockDao.findByBatchId(batchId)
                .groupBy { it.lockKey }

            db.requestedEntryDao.deleteBatch(batchId)

            LoadEntriesResult(
                entries = buildList {
                    addAll(entriesFromLruMap.values)
                    entriesToQueryDb.map {
                        CacheEntryAndLocks(
                            urlKey = it.requestedKey,
                            entry = entriesInDb[it.requestedKey],
                            locks = locksInDb[it.requestedKey] ?: emptyList()
                        )
                    }
                },
                pending = emptyList(),
                loadedFromDb = false,
            )
        }
    }


    private fun loadEntry(urlKey: String): CacheEntry? {
        return loadEntryAndLocks(urlKey).entry
    }

    private fun loadEntryAndLocks(urlKey: String): CacheEntryAndLocks {
        return lruMap.computeIfAbsent(urlKey) { key ->
            val entryInDb = db.cacheEntryDao.findEntryAndBodyByKey(urlKey)
            val entryLocks = db.retentionLockDao.findByKey(urlKey)
            CacheEntryAndLocks(
                urlKey = key,
                entry = entryInDb,
                locks = entryLocks
            )
        }
    }

    private fun upsertEntries(
        entries: List<CacheEntry>
    ) {
        entries.forEach {
            lruMap.compute(it.key) { key, prev ->
                prev?.copy(
                    entry = it
                ) ?: CacheEntryAndLocks(
                    urlKey = key,
                    entry = it,
                    locks = emptyList()
                )
            }
        }

        pendingCacheEntryUpdates.update { prev ->
            prev + entries
        }
    }


    override fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener?
    ): List<StoreResult> {
        val md5Digest = Md5Digest()
        val timeNow = systemTimeInMillis()
        val entryPaths = pathsProvider()

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
                    fileSystem.moveWithFallback(entryToStore.responseBodyTmpLocalPath, tmpFile)
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
                    MergedHeaders(IHttpHeaders.fromMap(overrideHeaders), response.headers)
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
            val requestEntries = entriesWithTmpFileAndIntegrityInfo.map {
                RequestedEntry(
                    requestSha256 = it.cacheEntry.integrity ?: "",
                    requestedKey = md5Digest.urlKey(it.cacheEntry.url),
                    batchId = batchId,
                )
            }

            val loadedEntriesLruResult = loadEntries(requestEntries,
                loadFromDb = false)
            val processEntriesFn: () -> List<CacheEntryInProgress> = {
                val loadedEntries = loadEntries(
                    requestEntries = entriesWithTmpFileAndIntegrityInfo.map {
                        RequestedEntry(
                            requestSha256 = it.cacheEntry.integrity ?: "",
                            requestedKey = md5Digest.urlKey(it.cacheEntry.url),
                            batchId = batchId,
                        )
                    },
                )

                val entriesInCache = loadedEntries.entries.mapNotNull { it.entry }
                val entriesWithLock = loadedEntries.entries.filter {
                    it.locks.isNotEmpty()
                }.map { it.urlKey }
                val entriesInCacheMap = entriesInCache.associateBy {
                    it.key
                }

                val entriesToSave = entriesWithTmpFileAndIntegrityInfo.map { entryInProgress ->
                    val storedEntry = entriesInCacheMap[entryInProgress.cacheEntry.key]
                    val storedEntryHeaders = storedEntry?.responseHeaders?.let {
                        IHttpHeaders.fromString(it)
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
                                    IHttpHeaders.fromMap(overrideHeaders),
                                    entryInProgress.responseHeaders,
                                    storedEntryHeaders,
                                ).asString()
                            )
                        )
                    }else {
                        //The new entry does not validate, so we will need to store the new body.
                        val destPaths = pathsProvider()
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
                        fileSystem.moveWithFallback(entryInProgress.tmpFile, destPath)

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

                upsertEntries(entriesToSave.map { it.cacheEntry } )

                /* For all StoreRequests where a lock has been requested, create the lock, put it in
                 * the Lru memory cache, and add it to the
                 *
                 */
                val locks = storeRequest.filter {
                    it.createRetentionLock
                }.map { entryToStore ->
                    val urlKey = md5Digest.urlKey(entryToStore.request.url)

                    urlKey to RetentionLock(
                        lockId = lockIdAtomic.incrementAndGet(),
                        lockKey = urlKey
                    ).also {
                        addLockToLruMap(it)
                    }
                }.also { keyAndLock ->
                    val newLocks = keyAndLock.map { it.second }
                    pendingLockUpserts.update { prev ->
                        prev + newLocks
                    }
                }.associate {
                    it.second.lockKey to it.second.lockId
                }

                entriesToSave.map { entry ->
                    entry.copy(
                        lockId = locks[entry.cacheEntry.key] ?: 0L
                    )
                }
            }

            val dbProcessedEntries = if(
                loadedEntriesLruResult.pending.isNotEmpty()
            ) {
                db.withDoorTransaction { processEntriesFn() }
            }else {
                processEntriesFn()
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
    override fun retrieve(request: IHttpRequest): IHttpResponse? {
        logger?.i(LOG_TAG, "$logPrefix Retrieve ${request.url}")

        val key = Md5Digest().urlKey(request.url)
        val entryAndLocks = loadEntryAndLocks(key)
        val entry = entryAndLocks.entry
        if(entry != null) {
            if(fileSystem.exists(Path(entry.storageUri))) {
                logger?.d(LOG_TAG, "$logPrefix FOUND ${request.url}")
                pendingLastAccessedUpdates.update { prev ->
                    prev + LastAccessedUpdate(key, systemTimeInMillis())
                }

                return CacheResponse(
                    fileSystem = fileSystem,
                    request = request,
                    headers = iHeadersBuilder {
                        takeFrom(IHttpHeaders.fromString(entry.responseHeaders))
                        header(HEADER_LAST_VALIDATED_TIMESTAMP, entry.lastValidated.toString())
                    },
                    storageUri = entry.storageUri,
                    httpResponseCode = entry.statusCode,
                    uncompressedSize = entry.uncompressedSize,
                )
            }else {
                logger?.d(LOG_TAG, "$logPrefix Entry deleted externally:  ${request.url}")
                if(entryAndLocks.locks.isEmpty()) {
                    logger?.d(LOG_TAG, "$logPrefix Entry deleted externally: " +
                            "${request.url} - has no locks, so removing from cache")

                    lruMap.computeIfPresent(key) { urlKey, prev ->
                        prev.copy(
                            entry = null
                        )
                    }

                    pendingCacheEntryUpdates.update { prev ->
                        prev.filter { it.key != key }
                    }

                    pendingCacheEntryDeletes.update { prev ->
                        prev + entry
                    }
                }else {
                    logger?.w(LOG_TAG, "$logPrefix Entry deleted externally: " +
                            "${request.url} - BUT IT HAD LOCKS!!! Not good!")
                }
            }
        }

        logger?.d(LOG_TAG, "$logPrefix MISS ${request.url}")
        return null
    }

    override fun updateLastValidated(validatedEntry: ValidatedEntry) {
        val md5 = Md5Digest()
        val timeNow = systemTimeInMillis()
        val urlKey = md5.urlKey(validatedEntry.url)

        loadEntry(urlKey)
        lruMap.compute(urlKey) { _, prevEntry ->
            val existingEntry = prevEntry?.entry
            if(existingEntry != null) {
                val existingHeaders = IHttpHeaders.fromString(existingEntry.responseHeaders)

                val newHeadersCorrected = validatedEntry.headers.mapHeaders { headerName, headerValue ->
                    when {
                        NOT_MODIFIED_IGNORE_HEADERS.any { headerName.equals(it, true) } -> null
                        else -> headerValue
                    }
                }
                val newHeaders = MergedHeaders(newHeadersCorrected, existingHeaders)
                val cacheEntryUpdated = existingEntry.copy(
                    responseHeaders = newHeaders.asString(),
                    lastValidated = timeNow,
                    lastAccessed = timeNow,
                )

                pendingCacheEntryUpdates.update { prev ->
                    prev + cacheEntryUpdated
                }

                prevEntry.copy(
                    entry = cacheEntryUpdated
                )
            }else {
                prevEntry
            }
        }
    }

    override fun getCacheEntry(url: String): CacheEntry? {
        return loadEntry(Md5Digest().urlKey(url))?.copy()
    }

    override fun getLocks(url: String): List<RetentionLock> {
        val urlKey = Md5Digest().urlKey(url)
        loadEntry(urlKey)
        return lruMap[urlKey]?.locks ?: emptyList()
    }

    override fun getEntries(urls: Set<String>): Map<String, CacheEntry> {
        val batchId = batchIdAtomic.incrementAndGet()
        val md5Digest = Md5Digest()

        val entryLoadResult = loadEntries(
            requestEntries = urls.map {  url ->
                RequestedEntry(
                    batchId = batchId,
                    requestedKey =  md5Digest.urlKey(url)
                )
            },
        )

        return entryLoadResult.entries.mapNotNull { entryAndLocks ->
            entryAndLocks.entry?.let {
                entryAndLocks.urlKey to it
            }
        }.toMap()
    }

    private fun CacheEntry.isStoredIn(parent: Path): Boolean {
        val currentPath = Path(storageUri)
        return currentPath.toString().startsWith(parent.toString())
    }

    /**
     * Used when an existing cache entry is locked or unlocked
     */
    private fun CacheEntry.moveToNewPath(destParent: Path): CacheEntry? {
        val currentPath = Path(storageUri)
        if(!fileSystem.exists(currentPath))
            return null //file with body no longer exists. Might have been deleted by OS.

        if(!fileSystem.exists(destParent)) {
            fileSystem.createDirectories(destParent)
        }

        return if(!currentPath.toString().startsWith(destParent.toString())) {
            val newDestPath = Path(destParent, currentPath.name)
            logger?.d(LOG_TAG, "$logPrefix moveToNewPath (${this.url}) $currentPath -> $newDestPath")
            fileSystem.moveWithFallback(currentPath, newDestPath)
            copy(storageUri = newDestPath.toString())
        }else {
            this
        }
    }

    private fun addLockToLruMap(retentionLock: RetentionLock): CacheEntryAndLocks {
        return lruMap.compute(retentionLock.lockKey) { urlKey, entryAndLocks ->
            entryAndLocks?.let { entryVal ->
                val isNewlyLocked = entryVal.locks.isEmpty()
                val persistentPath = pathsProvider().persistentPath

                entryVal.copy(
                    locks = entryVal.locks + retentionLock,
                    entry = entryVal.takeIf {
                        isNewlyLocked && it.entry?.isStoredIn(persistentPath) == false
                    }?.moveLock?.withLock {
                        entryVal.entry?.moveToNewPath(pathsProvider().persistentPath)
                    } ?: entryVal.entry
                )
            } ?: CacheEntryAndLocks(
                urlKey = urlKey,
                entry = null,
                locks = listOf(retentionLock)
            )
        } ?: throw IllegalStateException("Can't happen")
    }

    override fun addRetentionLocks(
        locks: List<EntryLockRequest>
    ): List<Pair<EntryLockRequest, RetentionLock>> {
        logger?.v(LOG_TAG) {
            "$logPrefix add retention locks for ${locks.joinToString { it.url } }"
        }
        val md5Digest = Md5Digest()
        loadEntries(
            requestEntries = locks.map { RequestedEntry(requestedKey = md5Digest.urlKey(it.url)) },
        )

        return locks.map { lockRequest ->
            val key = md5Digest.urlKey(lockRequest.url)
            val lock = RetentionLock(
                lockId = lockIdAtomic.incrementAndGet(),
                lockKey = key,
                lockRemark = lockRequest.remark,
            )

            Triple(lockRequest, lock, addLockToLruMap(lock))
        }.also { requestsAndLocks ->
            val newLockUpserts = requestsAndLocks.map { it.second }
            pendingLockUpserts.update { prev ->
                prev + newLockUpserts
            }

            val cacheEntriesToUpsert = requestsAndLocks.mapNotNull {
                it.third.entry
            }
            pendingCacheEntryUpdates.update { prev ->
                prev + cacheEntriesToUpsert
            }
        }.map {
            it.first to it.second
        }
    }

    /**
     * Lock removal is done by adding it to the pending list. This isn't urgent. This avoids a large
     * number of database transactions running when lots of small files are being uploaded
     */
    override fun removeRetentionLocks(locksToRemove: List<RemoveLockRequest>) {
        logger?.v(LOG_TAG) {
            "$logPrefix remove retention locks for ${locksToRemove.joinToString { "#${it.lockId}${it.url}" } }"
        }
        pendingLockRemovals.update { prev ->
            prev + locksToRemove.map { it.lockId }
        }
        val md5Digest = Md5Digest()
        val entriesWithLostLock = mutableListOf<CacheEntry>()

        locksToRemove.forEach { removeRequest ->
            lruMap.computeIfPresent(md5Digest.urlKey(removeRequest.url)) { key, prev ->
                val newLockList = prev.locks.filter { it.lockId != removeRequest.lockId }
                val isNewlyUnlocked = prev.locks.isNotEmpty() && newLockList.isEmpty()
                val cachePath = pathsProvider().cachePath

                prev.copy(
                    locks = prev.locks.filter { it.lockId != removeRequest.lockId },

                    entry = prev.takeIf {
                        isNewlyUnlocked && it.entry?.isStoredIn(cachePath) == false
                    }?.entry?.moveToNewPath(cachePath)?.also {
                        entriesWithLostLock += it
                    } ?: prev.entry
                )
            }
        }

        pendingCacheEntryUpdates.update { prev ->
            prev +  entriesWithLostLock
        }
    }

    fun commit() {
        val lastAccessUpdates = pendingLastAccessedUpdates.getAndUpdate {
            emptyList()
        }

        val lockUpsertsPending = pendingLockUpserts.getAndUpdate { emptyList() }

        val lockRemovalsPending = pendingLockRemovals.getAndUpdate {
            emptyList()
        }

        val cacheEntryUpserts = pendingCacheEntryUpdates.getAndUpdate {
            emptyList()
        }

        val cacheEntryDeletes = pendingCacheEntryDeletes.getAndUpdate {
            emptyList()
        }

        if(lastAccessUpdates.isEmpty() && lockRemovalsPending.isEmpty() &&
            cacheEntryUpserts.isEmpty() && lockUpsertsPending.isEmpty() &&
            cacheEntryDeletes.isEmpty()
        )
            return

        val updatesMap = mutableMapOf<String, Long>()
        lastAccessUpdates.forEach {
            updatesMap[it.key] = it.accessTime
        }

        db.withDoorTransaction {
            db.cacheEntryDao.delete(cacheEntryDeletes)
            db.cacheEntryDao.takeIf { cacheEntryUpserts.isNotEmpty() }
                ?.upsertList(cacheEntryUpserts)

            updatesMap.forEach {
                db.cacheEntryDao.updateLastAccessedTime(it.key, it.value)
            }

            db.retentionLockDao.upsertList(lockUpsertsPending)
            db.retentionLockDao.delete(lockRemovalsPending.map { RetentionLock(lockId = it) } )
        }
    }

    override fun close() {
        scope.cancel()
        commit()
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