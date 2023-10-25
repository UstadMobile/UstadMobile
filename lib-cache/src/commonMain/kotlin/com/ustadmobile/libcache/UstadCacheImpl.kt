package com.ustadmobile.libcache

import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.libcache.base64.encodeBase64
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.RequestedEntry
import com.ustadmobile.libcache.db.entities.ResponseBody
import com.ustadmobile.libcache.headers.CouponHeader.Companion.COUPON_ACTUAL_SHA_256
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.MimeTypeHelper
import com.ustadmobile.libcache.headers.asString
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.io.transferToAndGetSha256
import com.ustadmobile.libcache.io.unzipTo
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
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class UstadCacheImpl(
    private val fileSystem: FileSystem = SystemFileSystem,
    storagePath: Path,
    private val db: UstadCacheDb,
    internal val mimeTypeHelper: MimeTypeHelper,
) : UstadCache {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val tmpDir = Path(storagePath, "tmp")

    private val dataDir = Path(storagePath, "data")

    private val tmpCounter = atomic(0)

    private val batchIdAtomic = atomic(0)

    data class CacheEntryAndTmpFile(
        val cacheEntry: CacheEntry,
        val tmpFile: Path,
    )

    override fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener?
    ): List<StoreResult> {
        fileSystem.takeIf { !fileSystem.exists(dataDir) }?.createDirectories(dataDir)
        fileSystem.takeIf { !fileSystem.exists(tmpDir) }?.createDirectories(tmpDir)

        val cacheEntries = storeRequest.map { entryToStore ->
            val response = entryToStore.response
            val tmpFile = Path(tmpDir, "${tmpCounter.incrementAndGet()}.tmp")

            val bodySource = response.bodyAsSource()
                ?: throw IllegalArgumentException("Response for ${entryToStore.request.url} has no body")

            val sha256 = bodySource.transferToAndGetSha256(tmpFile).sha256
                .encodeBase64()

            val headersStr = headersBuilder {
                takeFrom(response.headers)
                header(COUPON_ACTUAL_SHA_256, sha256)
            }.asString()

            CacheEntryAndTmpFile(
                cacheEntry = CacheEntry(
                    url = entryToStore.request.url,
                    responseBodySha256 = sha256,
                    responseHeaders = headersStr,
                ),
                tmpFile = tmpFile
            )
        }

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

            val responseBodies = cacheEntries.filter { entryAndFile ->
                entryAndFile.cacheEntry.responseBodySha256.let { it != null && it in sha256sToStore }
            }.map {
                val destFile = Path(dataDir, randomUuid())
                fileSystem.atomicMove(it.tmpFile, destFile)

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

        return emptyList()
    }

    override fun storeZip(
        zipSource: Source,
        urlPrefix: String,
        retain: Boolean,
    ) {
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
                    }
                ),
                skipChecksum = true,
                retain = retain,
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
        val (entry, body) = db.cacheEntryDao.findEntryAndBodyByUrl(request.url) ?: return null
        if(entry != null && body != null) {
            return CacheResponse(
                fileSystem = fileSystem,
                request = request,
                headers = HttpHeaders.fromString(entry.responseHeaders),
                responseBody = body
            )
        }

        return null
    }

    override fun addRetentionLocks(locks: List<CacheRetentionLock>): List<Int> {
        TODO("Not yet implemented")
    }

    override fun removeRetentionLocks(lockIds: List<Int>) {
        TODO("Not yet implemented")
    }

    override suspend fun storeBlob(
        localDataUri: String,
        endpointUrl: String,
        retentionJoin: CacheRetentionJoin
    ): String {
        TODO("Not yet implemented")
    }


    override fun close() {
        scope.cancel()
    }
}