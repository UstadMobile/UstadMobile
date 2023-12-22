package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.upload.BlobBatchUploadUseCase.Companion.BLOB_RESPONSE_HEADER_PREFIX
import com.ustadmobile.core.domain.upload.ChunkedUploadUseCase
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.io.readTo

/**
 *
 */
class BlobBatchUploadUseCaseJvm(
    private val chunkedUploadUseCase: ChunkedUploadUseCase,
    private val httpClient: HttpClient,
    private val httpCache: UstadCache,
): BlobBatchUploadUseCase {

    private val uploadScope = CoroutineScope(Dispatchers.IO + Job())

    data class BlobAndResponse(
        val blob: BlobBatchUploadResponseItem,
        val response: HttpResponse,
    )

    private suspend fun asyncUploadItemsFromChannelProcessor(
        channel: ReceiveChannel<BlobAndResponse>,
        remoteUrl: String,
    ) = coroutineScope {
        async {
            for (item in channel) {
                chunkedUploadUseCase(
                    uploadUuid = item.blob.uploadUuid,
                    totalSize = item.response.request.headers["content-length"]?.toLong() ?: -1,
                    getChunk = { chunkInfo, buffer ->
                        val partialResponse = httpCache.retrieve(
                            requestBuilder(item.blob.blobUrl) {
                                header("Range", "bytes=${chunkInfo.start}-${chunkInfo.end + 1}")
                            }
                        ) ?: throw IllegalArgumentException("${item.blob.blobUrl} not in cache")

                        partialResponse.bodyAsSource()?.readTo(buffer, 0, chunkInfo.size)
                        if(chunkInfo.isLastChunk) {
                            ChunkedUploadUseCase.ChunkResponseInfo(
                                extraHeaders = partialResponse.headers.names().map { headerName ->
                                    "$BLOB_RESPONSE_HEADER_PREFIX$headerName" to
                                            partialResponse.headers.getAllByName(headerName)
                                }.toMap()
                            )
                        }else {
                            null
                        }
                    },
                    remoteUrl = remoteUrl,
                    fromByte = item.blob.fromByte
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(
        blobUrls: List<String>,
        batchUuid: String,
        endpoint: Endpoint,
        onProgress: (Int) -> Unit
    ) {
        val cacheResponses = blobUrls.associateWith { url ->
            httpCache.retrieve(requestBuilder(url))
        }
        val uploadRequest = cacheResponses.mapNotNull { entry ->
            val entrySize = entry.value?.headers?.get("content-lenth")?.toLong() ?: -1
            entry.value?.takeIf { entrySize > 0 }?.let {
                BlobBatchUploadRequestItem(
                    blobUrl = entry.key,
                    size = entrySize
                )
            }
        }

        withContext(uploadScope.coroutineContext) {
            val response: BlobBatchUploadResponse = httpClient.post("${endpoint.url}api/blob/upload-init") {
                setBody(uploadRequest)
            }.body()

            val blobsAndResponses = response.blobsToUpload.mapNotNull { blobItem ->
                cacheResponses[blobItem.blobUrl]?.let { httpResponse ->
                    BlobAndResponse(blobItem, httpResponse)
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
                    remoteUrl = "${endpoint.url}api/blob/upload"
                )
            }

            jobs.awaitAll()
        }
    }
}