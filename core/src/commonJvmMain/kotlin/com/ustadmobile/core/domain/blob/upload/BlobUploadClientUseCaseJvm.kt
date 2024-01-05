package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase.Companion.BLOB_RESPONSE_HEADER_PREFIX
import com.ustadmobile.core.domain.upload.ChunkInfo
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.io.readTo
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.transferjobitem.TransferJobItemStatusUpdater
import com.ustadmobile.core.domain.upload.ChunkedUploadClientChunkGetterUseCase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.libcache.response.requireHeadersContentLength
import io.github.aakira.napier.Napier
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Blob upload client implementation that uses local http cache (UstadCache from lib-cache).
 */
class BlobUploadClientUseCaseJvm(
    private val chunkedUploadUseCase: ChunkedUploadClientChunkGetterUseCase,
    private val httpClient: HttpClient,
    private val httpCache: UstadCache,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase,
    private val endpoint: Endpoint,
): BlobUploadClientUseCase {

    /*
     * Data class that has everything required by the fan-out processor.
     */
    data class UploadQueueItem(
        val blobUploadResponseItem: BlobUploadResponseItem,
        val blobUploadItem: BlobUploadClientUseCase.BlobTransferJobItem,
        val totalSize: Long,
        val chunkSize: Int,
    )

    class UploadNotCompleteException(message: String): Exception(message)

    /**
     * Get a chunk for upload by making an http range request to the cache.
     */
    inner class CacheResponseChunkGetter(
        private val url: String,
        private val batchUuid: String,
    ) : ChunkedUploadClientChunkGetterUseCase.UploadChunkGetter {

        override suspend fun invoke(
            chunk: ChunkInfo.Chunk,
            buffer: ByteArray
        ): ChunkedUploadClientUseCaseKtorImpl.ChunkResponseInfo? {
            val partialResponse = httpCache.retrieve(
                requestBuilder(url) {
                    //ChunkInfo.Chunk is exclusive, range bytes are inclusive.
                    //e.g. chunk to/from = 0-20,20-40...
                    // therefor range request = 0-19, 20-39...
                    header("Range", "bytes=${chunk.start}-${chunk.end - 1}")
                }
            ) ?: throw IllegalArgumentException("$url not in cache")

            partialResponse.bodyAsSource()?.readTo(buffer, 0, chunk.size)
            return if(chunk.isLastChunk) {
                ChunkedUploadClientUseCaseKtorImpl.ChunkResponseInfo(
                    extraHeaders = buildMap {
                        put(
                            key = BlobUploadClientUseCase.BLOB_UPLOAD_HEADER_BATCH_UUID,
                            value = listOf(batchUuid)
                        )
                        partialResponse.headers.names().filter { headerName ->
                            !DO_NOT_SEND_HEADERS.any { it.equals(headerName, true) }
                        }.forEach { headerName ->
                            put("$BLOB_RESPONSE_HEADER_PREFIX$headerName",
                                partialResponse.headers.getAllByName(headerName))
                        }
                    }
                )
            }else {
                null
            }
        }
    }


    private suspend fun asyncUploadItemsFromChannelProcessor(
        channel: ReceiveChannel<UploadQueueItem>,
        batchUuid: String,
        remoteUrl: String,
        onProgress: (BlobUploadClientUseCase.BlobUploadProgressUpdate) -> Unit,
        onStatusUpdate: (BlobUploadClientUseCase.BlobUploadStatusUpdate) -> Unit,
    ) = coroutineScope {
        async {
            for (queueItem in channel) {
                chunkedUploadUseCase(
                    uploadUuid = queueItem.blobUploadResponseItem.uploadUuid,
                    totalSize = queueItem.totalSize,
                    getChunk = CacheResponseChunkGetter(
                        url = queueItem.blobUploadResponseItem.blobUrl,
                        batchUuid = batchUuid,
                    ),
                    remoteUrl = remoteUrl,
                    fromByte = queueItem.blobUploadResponseItem.fromByte,
                    chunkSize = queueItem.chunkSize,
                    onProgress = { bytesUploaded ->
                        onProgress(
                            BlobUploadClientUseCase.BlobUploadProgressUpdate(
                                uploadItem = queueItem.blobUploadItem,
                                bytesTransferred = bytesUploaded,
                            )
                        )
                    },
                    onStatusChange = { status ->
                        onStatusUpdate(
                            BlobUploadClientUseCase.BlobUploadStatusUpdate(
                                uploadItem = queueItem.blobUploadItem,
                                status = status.value
                            )
                        )
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(
        blobUrls: List<BlobUploadClientUseCase.BlobTransferJobItem>,
        batchUuid: String,
        endpoint: Endpoint,
        onProgress: (BlobUploadClientUseCase.BlobUploadProgressUpdate) -> Unit,
        onStatusUpdate: (BlobUploadClientUseCase.BlobUploadStatusUpdate) -> Unit,
        chunkSize: Int,
    ) {
        val blobToUploadItemMap = blobUrls.associateBy {
            it.blobUrl
        }

        val uploadRequestItems = blobUrls.map { uploadItem ->
            val contentLength = httpCache.retrieve(
                requestBuilder(uploadItem.blobUrl)
            )?.requireHeadersContentLength() ?: throw IllegalArgumentException(
                "${uploadItem.blobUrl} not available in cache!"
            )
            BlobUploadRequestItem(
                blobUrl = uploadItem.blobUrl,
                size = contentLength
            )
        }

        val urlToRequestItemMap = uploadRequestItems.associateBy {
            it.blobUrl
        }

        coroutineScope {
            val response: BlobUploadResponse = httpClient.post("${endpoint.url}api/blob/upload-init-batch") {
                contentType(ContentType.Application.Json)
                setBody(
                    BlobUploadRequest(
                        blobs = uploadRequestItems,
                        batchUuid = batchUuid,
                    )
                )
            }.body()

            val blobsAndResponses = response.blobsToUpload.map { blobItem ->
                val blobUploadRequestItem = urlToRequestItemMap[blobItem.blobUrl]
                    ?: throw IllegalArgumentException("Server returned ${blobItem.blobUrl} that was not in request")
                val blobUploadItem = blobToUploadItemMap[blobItem.blobUrl]
                    ?: throw IllegalArgumentException("Internal error: ${blobItem.blobUrl} ")

                UploadQueueItem(
                    blobUploadResponseItem = blobItem,
                    blobUploadItem = blobUploadItem,
                    totalSize = blobUploadRequestItem.size,
                    chunkSize = chunkSize,
                )
            }

            val receiveChannel = produce(
                capacity = Channel.UNLIMITED
            ) {
                blobsAndResponses.forEach { send(it) }
                close()
            }

            //put all items on the channel
            val jobs = (0..4).map {
                asyncUploadItemsFromChannelProcessor(
                    channel = receiveChannel,
                    remoteUrl = "${endpoint.url}api/blob/upload-batch-data",
                    batchUuid = batchUuid,
                    onProgress = onProgress,
                    onStatusUpdate = onStatusUpdate,
                )
            }

            jobs.awaitAll()
        }
    }

    override suspend fun invoke(transferJobUid: Int) {
        val transferJob = db.transferJobDao.findByUid(transferJobUid)
            ?: throw IllegalArgumentException("BlobUpload: TransferJob #$transferJobUid does not exist")
        val transferJobItems = db.transferJobItemDao.findByJobUid(transferJobUid)
        val batchUuid = transferJob.tjUuid
            ?: throw IllegalArgumentException("TransferJob has no uuid")

        coroutineScope {
            val transferJobItemStatusUpdater = TransferJobItemStatusUpdater(db, repo, this)
            try {
                invoke(
                    blobUrls = transferJobItems.mapNotNull { jobItem ->
                        jobItem.tjiSrc?.let {
                            BlobUploadClientUseCase.BlobTransferJobItem(
                                blobUrl = it,
                                transferJobItemUid = jobItem.tjiUid,
                                lockIdToRelease = jobItem.tjiLockIdToRelease,
                            )
                        }
                    },
                    batchUuid = batchUuid,
                    endpoint = endpoint,
                    onProgress = transferJobItemStatusUpdater::onProgressUpdate,
                    onStatusUpdate = {
                        transferJobItemStatusUpdater.onStatusUpdate(it)
                        if(it.status == TransferJobItemStatus.STATUS_COMPLETE_INT &&
                            it.uploadItem.lockIdToRelease != 0
                        ) {
                            Napier.d { "BlobUploadUseCaseJvm: release cache lock #(${it.uploadItem.lockIdToRelease}) for ${it.uploadItem.blobUrl}" }
                            httpCache.removeRetentionLocks(listOf(it.uploadItem.lockIdToRelease))
                        }
                    },
                )

                val numIncompleteItems = db.withDoorTransactionAsync {
                    transferJobItemStatusUpdater.onFinished()
                    db.transferJobItemDao.findNumberJobItemsNotComplete(transferJobUid)
                }

                if(numIncompleteItems != 0) {
                    throw UploadNotCompleteException("Upload #$transferJobUid not complete: " +
                            "$numIncompleteItems TransferJobItem(s) pending")
                }
            }catch(e: Throwable) {
                Napier.e("BlobUploadClientUseCase: Exception. Attempt has failed.", e)

                withContext(NonCancellable) {
                    transferJobItemStatusUpdater.onFinished()
                }

                throw e
            }
        }
    }
    companion object {

        /**
         * Headers that should not be sent as part of the final upload chunk.
         * Content-Range: because the chunk getter makes a partial request from the cache, the
         *   response will include a content-range header from getting that specific chunk. This
         *   should not be included on the stored blob
         * Content-Length: because teh chunk getter makes a partial request from the cache, the
         *   response length will only be the length of the chunk. This is wrong. The server side
         *   will automatically determine the length based on the size uploaded.
         *
         */
        private val DO_NOT_SEND_HEADERS = listOf("content-range", "content-length")

        const val MAX_ATTEMPTS_DEFAULT = 5

        const val RETRY_WAIT_SECONDS = 10


    }

}