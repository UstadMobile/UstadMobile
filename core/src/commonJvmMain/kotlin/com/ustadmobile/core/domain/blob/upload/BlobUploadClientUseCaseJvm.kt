package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase.Companion.BLOB_RESPONSE_HEADER_PREFIX
import com.ustadmobile.core.domain.upload.ChunkInfo
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCase
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpResponse
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

class BlobUploadClientUseCaseJvm(
    private val chunkedUploadUseCase: ChunkedUploadClientUseCase,
    private val httpClient: HttpClient,
    private val httpCache: UstadCache,
    private val db: UmAppDatabase,
    private val endpoint: Endpoint,
): BlobUploadClientUseCase {

    data class BlobAndResponse(
        val blob: BlobUploadResponseItem,
        val response: HttpResponse,
        val chunkSize: Int,
    )

    /**
     * Get a chunk for upload by making an http range request to the cache.
     */
    inner class CacheResponseChunkGetter(
        private val url: String,
        private val batchUuid: String,
    ) : ChunkedUploadClientUseCase.UploadChunkGetter{

        override suspend fun invoke(
            chunk: ChunkInfo.Chunk,
            buffer: ByteArray
        ): ChunkedUploadClientUseCase.ChunkResponseInfo? {
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
                ChunkedUploadClientUseCase.ChunkResponseInfo(
                    extraHeaders = buildMap {
                        put(
                            key = BlobUploadClientUseCase.BLOB_UPLOAD_HEADER_BATCH_UUID,
                            value = listOf(batchUuid)
                        )
                        partialResponse.headers.names().forEach { headerName ->
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
        channel: ReceiveChannel<BlobAndResponse>,
        batchUuid: String,
        remoteUrl: String,
    ) = coroutineScope {
        async {
            for (item in channel) {
                chunkedUploadUseCase(
                    uploadUuid = item.blob.uploadUuid,
                    totalSize = item.response.headers["content-length"]?.toLong() ?: -1,
                    getChunk = CacheResponseChunkGetter(
                        url = item.blob.blobUrl,
                        batchUuid = batchUuid,
                    ),
                    remoteUrl = remoteUrl,
                    fromByte = item.blob.fromByte,
                    chunkSize = item.chunkSize,
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(
        blobUrls: List<String>,
        batchUuid: String,
        endpoint: Endpoint,
        onProgress: (Int) -> Unit,
        chunkSize: Int,
    ) {
        val cacheResponses = blobUrls.associateWith { url ->
            httpCache.retrieve(requestBuilder(url))
        }
        val uploadRequestItems = cacheResponses.mapNotNull { entry ->
            val entrySize = entry.value?.headers?.get("content-length")?.toLong() ?: -1
            entry.value?.takeIf { entrySize > 0 }?.let {
                BlobUploadRequestItem(
                    blobUrl = entry.key,
                    size = entrySize
                )
            }
        }

        coroutineScope {
            val response: BlobUploadResponse = httpClient.post("${endpoint.url}api/blob/upload-init") {
                contentType(ContentType.Application.Json)
                setBody(
                    BlobUploadRequest(
                        blobs = uploadRequestItems,
                        batchUuid = batchUuid,
                    )
                )
            }.body()

            val blobsAndResponses = response.blobsToUpload.mapNotNull { blobItem ->
                cacheResponses[blobItem.blobUrl]?.let { httpResponse ->
                    BlobAndResponse(blob = blobItem, response = httpResponse, chunkSize = chunkSize)
                }
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
                    remoteUrl = "${endpoint.url}api/blob/upload",
                    batchUuid = batchUuid,
                )
            }

            jobs.awaitAll()
        }
    }


    override suspend fun invoke(transferJobUid: Int) {
        val transferJob = db.transferJobDao.findByUid(transferJobUid)
            ?: throw IllegalArgumentException("BlobUpload: TransferJob #$transferJobUid does not exist")
        val transferJobItems = db.transferJobItemDao.findByJobUid(transferJobUid)
        invoke(
            blobUrls = transferJobItems.mapNotNull {
                it.tjiSrc
            },
            batchUuid =transferJob.tjUuid!!,
            endpoint = endpoint,
            onProgress = {

            }
        )
    }
}