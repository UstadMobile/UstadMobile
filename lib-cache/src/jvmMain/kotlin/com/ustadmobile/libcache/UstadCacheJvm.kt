package com.ustadmobile.libcache

import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.libcache.headers.CouponHeader.Companion.COUPON_HEADER_NAME
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.RequestedEntry
import com.ustadmobile.libcache.db.entities.ResponseBody
import com.ustadmobile.libcache.headers.CouponHeader
import com.ustadmobile.libcache.headers.HttpHeader
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.appendOrReplace
import com.ustadmobile.libcache.headers.toHeaderString
import com.ustadmobile.libcache.headers.toHeadersList
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.response.CacheResponseJvm
import com.ustadmobile.libcache.response.HttpResponse
import com.ustadmobile.libcache.response.bodyAsStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class UstadCacheJvm(
    private val cacheDir: File,
    private val db: UstadCacheDb
) : UstadCache {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val tmpDir = File(cacheDir, "tmp")

    private val dataDir = File(cacheDir, "data")

    private val tmpCounter = AtomicInteger(0)

    private val batchIdAtomic = AtomicInteger(0)

    data class CacheEntryAndTmpFile(
        val cacheEntry: CacheEntry,
        val tmpFile: File,
    )

    override suspend fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener
    ): List<StoreResult> = withContext(scope.coroutineContext) {
        dataDir.takeIf { !it.exists() }?.mkdirs()
        tmpDir.takeIf { !it.exists() }?.mkdirs()

        val digest = MessageDigest.getInstance("SHA-256")
        val cacheEntries = storeRequest.map { entryToStore ->
            val response = entryToStore.response
            val tmpFile = File(tmpDir, "${tmpCounter.incrementAndGet()}.tmp")

            DigestInputStream(response.bodyAsStream(), digest).use { inputStream ->
                FileOutputStream(tmpFile).use { fileOut ->
                    inputStream.copyTo(fileOut)
                    fileOut.flush()
                }
            }

            val sha256 = Base64.getEncoder().encodeToString(digest.digest())
            digest.reset()

            //Add the actual-sha256
            val responseCouponHeader = response.headers[COUPON_HEADER_NAME]?.let {
                CouponHeader.fromString(it).copy(
                    actualSha256 = sha256
                )
            } ?: CouponHeader(actualSha256 = sha256)

            CacheEntryAndTmpFile(
                cacheEntry = CacheEntry(
                    url = entryToStore.request.url,
                    responseBodySha256 = sha256,
                    responseHeaders = response.headers.toHeadersList().appendOrReplace(
                        name = COUPON_HEADER_NAME,
                        value = responseCouponHeader.toHeaderString(),
                    ).toHeaderString(),
                ),
                tmpFile = tmpFile
            )
        }

        //find the sha256's we don't have. If we have it, discard tmpFile. If we don't move tmp file to data dir
        val batchId = batchIdAtomic.incrementAndGet()

        db.withDoorTransactionAsync {
            db.requestedEntryDao.insertListAsync(cacheEntries.map {
                RequestedEntry(
                    requestSha256 = it.cacheEntry.responseBodySha256 ?: "",
                    requestedUrl = it.cacheEntry.url,
                    batchId = batchId,
                )
            })

            val sha256sToStore = db.requestedEntryDao.findSha256sNotPresent(batchId).toSet()
            val storedEntries = db.cacheEntryDao.findByRequestBatchId(batchId)
            val storedUrls = storedEntries.map { it.url }.toSet()

            val responseBodies = cacheEntries.filter {
                it.cacheEntry.responseBodySha256.let { it != null && it in sha256sToStore }
            }.map {
                val destFile = File(dataDir, UUID.randomUUID().toString())
                it.tmpFile.renameTo(destFile) //Assert here

                ResponseBody(
                    sha256 = it.cacheEntry.responseBodySha256!!,
                    storageUri = destFile.toURI().toString(),
                )
            }

            db.responseBodyDao.insertListAsync(responseBodies)

            val entriesToStore = cacheEntries.filter {
                it.cacheEntry.url !in storedUrls
            }.map {
                it.cacheEntry
            }

            db.cacheEntryDao.insertListAsync(entriesToStore)
            //TODO: Handle updating entries
            db.requestedEntryDao.deleteBatch(batchId)
        }

        emptyList()
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
    override suspend fun retrieve(request: HttpRequest): HttpResponse? {
        val (entry, body) = db.cacheEntryDao.findEntryAndBodyByUrl(request.url) ?: return null
        if(entry != null && body != null) {
            return CacheResponseJvm(request, HttpHeaders.fromString(entry.responseHeaders), body)
        }

        return null
    }

    override suspend fun addRetentionLocks(locks: List<CacheRetentionLock>): List<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun removeRetentionLocks(lockIds: List<Int>) {
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