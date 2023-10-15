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
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.CacheResponseJvm
import com.ustadmobile.libcache.response.HttpFileResponse
import com.ustadmobile.libcache.response.HttpResponse
import com.ustadmobile.libcache.response.bodyAsStream
import com.ustadmobile.libcache.uri.IUri
import com.ustadmobile.libcache.uri.IUriHelper
import com.ustadmobile.libcache.uri.UriHelperJvm
import com.ustadmobile.libcache.uri.UriJvm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import java.io.File
import java.io.FileOutputStream
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.Base64
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class UstadCacheJvm(
    cacheDir: File,
    private val db: UstadCacheDb,
    internal val mimeTypeHelper: MimeTypeHelper = FileMimeTypeHelperImpl(),
    internal val uriHelper: IUriHelper = UriHelperJvm(mimeTypeHelper),
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

    override fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener?
    ): List<StoreResult> {
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

        db.withDoorTransaction {
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

    /**
     * Store all entries from a given Zip as entries in the cache. This is useful to process zipped
     * content e.g. epubs, xAPI/Scorm files, etc.
     *
     * @param zipUri Uri providing an InputStream
     * @param urlPrefix should end with /
     */
    fun storeZip(
        zipUri: IUri,
        urlPrefix: String,
        retain: Boolean = true,
    ) {
        data class UnzippedEntry(
            val entry: ZipEntry,
            val tmpFile: File,
            val sha256: ByteArray,
        )

        val unzippedEntries = LinkedList<UnzippedEntry>()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        tmpDir.takeIf { !it.exists() }?.mkdirs()

        ZipInputStream(uriHelper.openInputStream(zipUri)).use { zipInput ->
            lateinit var zipEntry: ZipEntry
            while(zipInput.nextEntry?.also { zipEntry = it } != null) {
                if(zipEntry.name.endsWith("/"))
                    continue

                val tmpFile = File(tmpDir, UUID.randomUUID().toString())
                DigestOutputStream(FileOutputStream(tmpFile), messageDigest).use { outStream ->
                    zipInput.copyTo(outStream)
                    outStream.flush()
                }

                unzippedEntries.add(UnzippedEntry(
                    entry = zipEntry,
                    tmpFile = tmpFile,
                    sha256 = messageDigest.digest(),
                ))
            }
        }

        val entriesToCache = unzippedEntries.map {
            val request = requestBuilder {
                url = "$urlPrefix${it.entry.name}"
            }
            CacheEntryToStore(
                request = request,
                response = HttpFileResponse(
                    file = it.tmpFile,
                    mimeType = mimeTypeHelper.guessByExtension(it.entry.name.substringAfterLast("."))
                        ?: "application/octet-stream",
                    request = request,
                    extraHeaders = headersBuilder {
                        header(COUPON_ACTUAL_SHA_256, it.sha256.encodeBase64())
                    }
                ),
                responseBodyTmpLocalUri = UriJvm(it.tmpFile.toURI()),
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
            return CacheResponseJvm(request, HttpHeaders.fromString(entry.responseHeaders), body)
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