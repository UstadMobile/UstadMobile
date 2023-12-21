package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.domain.blob.upload.BlobBatchUploadUseCase.Companion.BLOB_RESPONSE_HEADER_PREFIX
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import io.github.reactivecircus.cache4k.Cache
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Manages the HTTP (server) blob batch upload endpoint on the server side. The class can be used by
 * any HTTP server as needed (e.g. Ktor, NanoHTTPD, etc).
 */
class BlobBatchUploadEndpoint(
    private val httpCache: UstadCache,
    private val tmpDir: Path,
    private val json: Json,
    private val fileSystem: FileSystem = SystemFileSystem,
    responseCacheSize: Int = 100
) {

    //Basic in-memory cache to avoid the need to read/parse the JSON from the disk every time a
    //blob upload from within the same batch is completed
    private val responseCache =
        Cache.Builder<String, BlobBatchUploadResponse>()
            .maximumCacheSize(responseCacheSize.toLong())
            .build()

    private suspend fun loadResponse(
        batchUuid: String
    ): BlobBatchUploadResponse {
        return responseCache.get(
            key = batchUuid
        ) {
            val batchPath = Path(tmpDir, batchUuid)
            val existingResponsePath = Path(batchPath, RESPONSE_JSON_FILENAME)
            if(fileSystem.exists(existingResponsePath)) {
                json.decodeFromString(
                    deserializer = BlobBatchUploadResponse.serializer(),
                    string = fileSystem.source(existingResponsePath).buffered().readString()
                )
            }else {
                BlobBatchUploadResponse(emptyList())
            }
        }
    }

    /**
     * The client will make a request to startSession with a BlobBatchUploadRequest. This could be an
     * entirely new request, or it could be resuming an existing request.
     *
     * The response will give the client UUIDs for each upload (e.g. to use with UploadRoute) and
     * the starting byte for each blob.
     *
     * @param request the BlobBatchUploadRequest that includes a list of the blobs that the client
     *        wishes to upload
     * @return a BlobBatchUploadResponse that lists the items that are needed (any urls that
     *        are already stored in the cache will not be included) and the starting position for each
     *        (in case the upload is being resumed and a partial upload is already there)
     */
    suspend fun startSession(
        request: BlobBatchUploadRequest
    ) : BlobBatchUploadResponse{
        //Ensure that this is a validated UUID e.g. filter malicious or invalid paths
        UUID.fromString(request.batchUuid)

        val urlsList = request.blobUrls.map {
            it.blobUrl
        }
        val batchPath = Path(tmpDir, request.batchUuid)
        val urlsStatus = httpCache.hasEntries(urlsList.toSet())
        val existingResponse = loadResponse(request.batchUuid)

        val existingResponseMap = existingResponse
            .blobsToUpload.associateBy { it.blobUrl }

        val newResponse = BlobBatchUploadResponse(
            request.blobUrls.mapNotNull { blobToUploadRequest ->
                //Response will only include those items that not yet cached
                if(urlsStatus[blobToUploadRequest.blobUrl] != true) {
                    val existingResponseItem =
                        existingResponseMap[blobToUploadRequest.blobUrl]
                    val uploadUuid = existingResponseItem?.uploadUuid
                        ?: UUID.randomUUID().toString()
                    val existingResponseFilePath = Path(batchPath, uploadUuid)

                    BlobBatchUploadResponseItem(
                        blobUrl = blobToUploadRequest.blobUrl,
                        uploadUuid = uploadUuid,
                        fromByte = fileSystem.metadataOrNull(existingResponseFilePath)?.size ?: 0L
                    )
                }else {
                    null
                }
            }
        )
        responseCache.put(request.batchUuid, newResponse)

        return newResponse
    }

    /**
     * When the uploading of a given blob is finished this function will be called on the final
     * chunk (e.g. it will be called by onUploadCompleted parameter of the UploadRoute etc).
     *
     * It will store the content of the blob in the httpCache.
     *
     * @param batchUuid the upload batch uuid
     * @param uploadUuid uuid for the individual blob as per BlobToUploadResponse
     * @param bodyPath the path where the blob has been stored (temporary file, will be moved, not
     *        copied, into the cache)
     * @param requestHeaders the headers from the final chunk upload request. Headers that should be
     *        added to the response (e.g. as it will be stored by the cache) should be uploaded with
     *        the prefix "X-Blob-Response-" (BLOB_RESPONSE_HEADER_PREFIX) e.g. to set the
     *        Content-Type header the final chunk upload request should include the header
     *        X-Blob-Response-Content-Type.
     */
    suspend fun onBlobFinished(
        batchUuid: String,
        uploadUuid: String,
        bodyPath: Path,
        requestHeaders: HttpHeaders
    ) {
        UUID.fromString(batchUuid)
        val batchResponse = loadResponse(batchUuid)
        val blobToUploadResponse = batchResponse.blobsToUpload.firstOrNull {
            it.uploadUuid == uploadUuid
        } ?: throw IllegalArgumentException("Upload $uploadUuid is not part of batch $batchUuid")


        val request = requestBuilder(blobToUploadResponse.blobUrl)
        val mimeType = requestHeaders["${BLOB_RESPONSE_HEADER_PREFIX}content-type"]
            ?: "application/octet-stream"

        httpCache.store(
            listOf(
                CacheEntryToStore(
                    request = request,
                    response = HttpPathResponse(
                        path = bodyPath,
                        fileSystem = fileSystem,
                        mimeType = mimeType,
                        request = request,
                        extraHeaders = headersBuilder {
                            requestHeaders.names().filter {
                                it.startsWith(BLOB_RESPONSE_HEADER_PREFIX)
                            }.forEach { headerName ->
                                header(
                                    name = headerName,
                                    value = requestHeaders[headerName]!!
                                        .removePrefix(BLOB_RESPONSE_HEADER_PREFIX)
                                )
                            }
                        }
                    ),
                    responseBodyTmpLocalPath = bodyPath,
                )
            )
        )

    }

    companion object {

        /**
         * Each upload batch will have a directory. Inside that directory the endpoint will save a
         * JSON to map the blob url to a unique upload uuid.
         */
        const val RESPONSE_JSON_FILENAME = ".batch-blob-upload.json"

    }

}