package com.ustadmobile.libcache

import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libcache.UstadCache.Companion.HEADER_LAST_VALIDATED_TIMESTAMP
import com.ustadmobile.libcache.base64.encodeBase64
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.RequestedEntry
import com.ustadmobile.libcache.db.entities.ResponseBody
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
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.CacheResponse
import com.ustadmobile.libcache.response.HttpPathResponse
import com.ustadmobile.libcache.response.HttpResponse
import com.ustadmobile.libcache.uuid.randomUuid
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class UstadCacheImpl(
    private val fileSystem: FileSystem = SystemFileSystem,
    storagePath: Path,
    private val db: UstadCacheDb,
    internal val mimeTypeHelper: MimeTypeHelper,
    private val logger: UstadCacheLogger? = null,
    private val listener: UstadCache.CacheListener? = null,
) : UstadCache {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val tmpDir = Path(storagePath, "tmp")

    private val dataDir = Path(storagePath, "data")

    private val tmpCounter = atomic(0)

    private val batchIdAtomic = atomic(0)

    private val logPrefix = "UstadCache($storagePath):"

    data class CacheEntryAndTmpFile(
        val cacheEntry: CacheEntry,
        val tmpFile: Path,
    )

    override fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener?
    ): List<StoreResult> {
        try {
            logger?.d(LOG_TAG) { "$logPrefix storerequest ${storeRequest.size} entries" }
            fileSystem.takeIf { !fileSystem.exists(dataDir) }?.createDirectories(dataDir)
            fileSystem.takeIf { !fileSystem.exists(tmpDir) }?.createDirectories(tmpDir)

            val cacheEntries = storeRequest.map { entryToStore ->
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
                        url = entryToStore.request.url,
                        responseBodySha256 = sha256,
                        statusCode = entryToStore.response.responseCode,
                        responseHeaders = headersStr,
                        cacheFlags = cacheFlags,
                        lastValidated = systemTimeInMillis(),
                    ),
                    tmpFile = tmpFile
                )
            }

            logger?.v(LOG_TAG) { "$logPrefix cacheEntries created ${cacheEntries.size} entries" }
            //find the sha256's we don't have. If we have it, discard tmpFile. If we don't move tmp file to data dir
            val batchId = batchIdAtomic.incrementAndGet()

            db.withDoorTransaction { _ ->
                db.requestedEntryDao.insertList(cacheEntries.map {
                    RequestedEntry(
                        requestSha256 = it.cacheEntry.responseBodySha256 ?: "",
                        requestedUrl = it.cacheEntry.url,
                        batchId = batchId,
                    )
                })

                val sha256sToStore = db.requestedEntryDao.findSha256sNotPresent(batchId).toSet()
                val storedEntries = db.cacheEntryDao.findByRequestBatchId(batchId)
                val storedUrls = storedEntries.map { it.url }.toSet()
                logger?.v(LOG_TAG) { "$logPrefix storing ${sha256sToStore.size} new sha256s" }

                val responseBodies = cacheEntries.filter { entryAndFile ->
                    entryAndFile.cacheEntry.responseBodySha256.let { it != null && it in sha256sToStore }
                }.map {
                    val destFile = Path(dataDir, randomUuid())
                    fileSystem.atomicMove(it.tmpFile, destFile)
                    logger?.v(LOG_TAG, "$logPrefix saved body data (SHA256=${it.cacheEntry.responseBodySha256!!}) to $destFile")

                    ResponseBody(
                        sha256 = it.cacheEntry.responseBodySha256!!,
                        storageUri = destFile.toString(),
                    )
                }


                db.responseBodyDao.insertList(responseBodies)

                val newEntries = cacheEntries.filter {
                    it.cacheEntry.url !in storedUrls
                }.map {
                    it.cacheEntry
                }

                db.cacheEntryDao.insertList(newEntries)
                //TODO: Handle updating entries
                db.requestedEntryDao.deleteBatch(batchId)
            }
            logger?.d(LOG_TAG, "$logPrefix db transaction completed")
            listener?.onEntriesStored(storeRequest)

            return emptyList()
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
     * In future, this could be streamed gradually via a RandomAccessFile
     *
     * NOTE: If we know in advance that a particular batch is going to be requested, we can run
     * a statusCheckCache to avoid running 100s-1000+ SQL queries for tiny jsons etc.
     *
     */
    override fun retrieve(request: HttpRequest): HttpResponse? {
        logger?.i(LOG_TAG, "$logPrefix Retrieve ${request.url}")

        /*
         * If a response was marked as static, then we will bust cache busting. See doc
         * on Coupon-Static header.
         */
        val queryParamIndex = request.url.indexOf("?")
        val requestWithoutQuery = if(queryParamIndex != -1)
            request.url.substring(0, queryParamIndex)
        else
            null

        val (entry, body) = db.cacheEntryDao.findEntryAndBodyByUrl(
            request.url, requestWithoutQuery) ?: return null
        if(entry != null && body != null) {
            logger?.d(LOG_TAG, "$logPrefix FOUND ${request.url}")
            return CacheResponse(
                fileSystem = fileSystem,
                request = request,
                headers = headersBuilder {
                    takeFrom(HttpHeaders.fromString(entry.responseHeaders))
                    header(HEADER_LAST_VALIDATED_TIMESTAMP, entry.lastValidated.toString())
                },
                responseBody = body,
                httpResponseCode = entry.statusCode
            )
        }

        logger?.d(LOG_TAG, "$logPrefix MISS ${request.url}")
        return null
    }


    override fun hasEntries(urls: Set<String>): Map<String, Boolean> {
        val batchId = batchIdAtomic.incrementAndGet()
        val urlsNotPresent = db.withDoorTransaction {
            db.requestedEntryDao.insertList(
                urls.map {  url ->
                    RequestedEntry(batchId = batchId, requestedUrl =  url)
                }
            )
            db.requestedEntryDao.findUrlsNotPresent(batchId).also {
                db.requestedEntryDao.deleteBatch(batchId)
            }
        }

        return urls.associateWith { url ->
            (url !in urlsNotPresent)
        }
    }

    override fun addRetentionLocks(locks: List<CacheRetentionLock>): List<Int> {
        TODO("Not yet implemented")
    }

    override fun removeRetentionLocks(lockIds: List<Int>) {
        TODO("Not yet implemented")
    }

    override fun close() {
        scope.cancel()
    }

    companion object {
        const val LOG_TAG = "UstadCache"
    }
}