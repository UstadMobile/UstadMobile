package com.ustadmobile.libcache.okhttp

import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCache.Companion.HEADER_FIRST_STORED_TIMESTAMP
import com.ustadmobile.libcache.UstadCache.Companion.HEADER_LAST_VALIDATED_TIMESTAMP
import com.ustadmobile.libcache.UstadCacheImpl.Companion.LOG_TAG
import com.ustadmobile.libcache.cachecontrol.CacheControlFreshnessChecker
import com.ustadmobile.libcache.cachecontrol.CacheControlFreshnessCheckerImpl
import com.ustadmobile.libcache.cachecontrol.RequestCacheControlHeader
import com.ustadmobile.libcache.cachecontrol.ResponseCacheabilityChecker
import com.ustadmobile.libcache.cachecontrol.ResponseCacheabilityCheckerImpl
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.logging.UstadCacheLogger
import com.ustadmobile.libcache.response.HttpPathResponse
import com.ustadmobile.libcache.response.HttpResponse
import kotlinx.io.asInputStream
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.internal.headersContentLength
import okio.buffer
import okio.source
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.Executors

/**
 * The OKHttp standard cache is a final class that we cannot customize or otherwise work with. So
 * we need an interceptor to implement the additional behaviors (as per README.md). This is an
 * application interceptor.
 */
class UstadCacheInterceptor(
    private val cache: UstadCache,
    private val cacheDir: File,
    private val logger : UstadCacheLogger? = null,
    private val cacheControlFreshnessChecker: CacheControlFreshnessChecker =
        CacheControlFreshnessCheckerImpl(),
    private val responseCacheabilityChecker: ResponseCacheabilityChecker =
        ResponseCacheabilityCheckerImpl(),
    private val fileSystem: FileSystem = SystemFileSystem,
): Interceptor {

    private val executor = Executors.newCachedThreadPool()

    private val logPrefix: String = "OKHttp-CacheInterceptor: "

    @Volatile
    private var cacheDirChecked = false

    /**
     * This runnable will simultaneously write.
     */
    inner class ReadAndCacheRunnable(
        private val call: Call,
        private val response: Response,
        private val pipeOut: PipedOutputStream,
    ) : Runnable{
        override fun run() {
            val buffer = ByteArray(8192)
            var bytesRead = 0
            val digest = MessageDigest.getInstance("SHA-256")

            try {
                val responseInStream = response.body?.byteStream()?.let {
                    DigestInputStream(it, digest)
                } ?: throw IllegalStateException()

                responseInStream.use { responseIn ->
                    if(!cacheDirChecked) {
                        cacheDir.takeIf { !it.exists() }?.mkdirs()
                        cacheDirChecked = true
                    }

                    val file = File(cacheDir, UUID.randomUUID().toString())
                    val fileOutStream = file.outputStream()
                    while(!call.isCanceled() &&
                        responseIn.read(buffer).also { bytesRead = it } != -1
                    ) {
                        fileOutStream.write(buffer, 0, bytesRead)
                        pipeOut.write(buffer, 0, bytesRead)
                    }
                    pipeOut.flush()
                    pipeOut.close()
                    fileOutStream.flush()
                    fileOutStream.close()

                    val cacheRequest = call.request().asCacheHttpRequest()

                    if(!call.isCanceled()) {
                        cache.store(listOf(
                            CacheEntryToStore(
                                request = cacheRequest,
                                response = HttpPathResponse(
                                    path = Path(file.absolutePath),
                                    fileSystem = fileSystem,
                                    mimeType = response.header("content-type") ?: "application/octet-stream",
                                    request = cacheRequest,
                                    extraHeaders = headersBuilder {
                                        takeFrom(response.headers.asCacheHttpHeaders())
                                    }
                                ),
                                responseBodyTmpLocalPath = Path(file.absolutePath)
                            )
                        ))
                    }
                }
            }catch(e: Throwable){
                logger?.e(LOG_TAG, "$logPrefix ReadAndCacheRunnable: exception handling ${call.request().url}", e)
                throw e
            }finally {
                response.close()
            }
        }
    }

    /**
     * Create a new OKHttp response that will simultaneously stream the response to the client (via
     * PipedInput/PipedOutput) AND save the response data to disk. Once the response has been fully
     * read, it will be stored in the cache.
     *
     * If/when required, when we need to handle partial downloads, the client could send a keep-partial
     * responses header and resume-from header.
     */
    private fun newCacheAndStoreResponse(
        response: Response,
        call: Call,
    ): Response {
        val pipeInStream = PipedInputStream()
        val pipeOutStream = PipedOutputStream(pipeInStream)
        try {
            val returnResponse = response.newBuilder().body(
                pipeInStream.source().buffer().asResponseBody(
                    (response.header("content-type") ?: "application/octet-stream").toMediaType(),
                    response.headersContentLength(),
                )
            ).build()
            executor.submit(ReadAndCacheRunnable(call, response, pipeOutStream))
            return returnResponse
        }catch(e: Throwable) {
            throw e
        }
    }

    private fun newResponseFromCachedResponse(
        cacheResponse: HttpResponse,
        call: Call
    ) : Response {
        val responseMediaType = cacheResponse.headers["content-type"]?.toMediaTypeOrNull()
            ?: "application/octet-stream".toMediaType()
        val responseBody = cacheResponse.bodyAsSource()?.asInputStream()?.source()
            ?.buffer()?.asResponseBody(
                contentType = responseMediaType,
                contentLength = cacheResponse.headers["content-length"]?.toLong() ?: -1
            )

        return Response.Builder()
            .headers(cacheResponse.headers.asOkHttpHeaders())
            .request(call.request())
            .body(responseBody)
            .code(200)
            .protocol(Protocol.HTTP_1_1)
            .message("OK")
            .build()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val call = chain.call()
        logger?.v(LOG_TAG, "$logPrefix intercept: ${request.method} $url")
        val requestHeaders = request.headers.asCacheHttpHeaders()
        val requestCacheControlHeader = requestHeaders["cache-control"]?.let {
            RequestCacheControlHeader.parse(it)
        }

        //If there is no chance of being able to cache (not http get or using no-store in request)
        if(!request.mightBeCacheable(requestCacheControlHeader)) {
            return chain.proceed(request)
        }

        val cacheRequest = request.asCacheHttpRequest()
        val cacheResponse = cache.retrieve(cacheRequest)

        val cachedResponseStatus = cacheResponse?.let {
            cacheControlFreshnessChecker(
                requestHeaders = request.headers.asCacheHttpHeaders(),
                requestDirectives = requestCacheControlHeader,
                responseHeaders = cacheResponse.headers,
                responseFirstStoredTime = cacheResponse.headers[HEADER_FIRST_STORED_TIMESTAMP]?.toLong()
                    ?: systemTimeInMillis(),
                responseLastValidated = cacheResponse.headers[HEADER_LAST_VALIDATED_TIMESTAMP]?.toLong()
                    ?: systemTimeInMillis()
            )
        }

        return when {
            /*
             * When response isFresh - can immediately return the cached response
             */
            cacheResponse != null && cachedResponseStatus?.isFresh == true -> {
                logger?.d(LOG_TAG, "$logPrefix HIT(valid) $url")
                newResponseFromCachedResponse(cacheResponse, call)
            }

            /**
             * The request header specified only-if-cached, but we don't have it.
             */
            requestCacheControlHeader?.onlyIfCached == true -> {
                Response.Builder()
                    .request(request)
                    .code(504)
                    .build()
            }

            /*
             * When response is not fresh, but can be validated, then send a validation request
             * and use the cached response if the
             */
            cachedResponseStatus != null && cachedResponseStatus.canBeValidated -> {
                val validateRequestBuilder = request.newBuilder()
                cachedResponseStatus.ifNoneMatch?.also {
                    validateRequestBuilder.addHeader("if-none-match", it)
                }
                cachedResponseStatus.ifNotModifiedSince?.also {
                    validateRequestBuilder.addHeader("if-modified-since", it)
                }
                val validationResponse = chain.proceed(validateRequestBuilder.build())
                if(validationResponse.code == 304) {
                    logger?.d(LOG_TAG, "$logPrefix HIT(validated) $url")
                    validationResponse.close()
                    //TODO: update the cache so it knows it is fresh
                    newResponseFromCachedResponse(cacheResponse, call)
                }else {
                    logger?.d(LOG_TAG, "$logPrefix MISS(invalid) $url")
                    if(responseCacheabilityChecker.canStore(validationResponse)) {
                        logger?.v(LOG_TAG, "$logPrefix $url can store - creating newCacheAndStore response")
                        newCacheAndStoreResponse(validationResponse, call)
                    }else {
                        logger?.v(LOG_TAG, "$logPrefix $url cannot store - returning response as-is")
                        validationResponse
                    }
                }
            }

            else -> {
                //Nothing in cache matches request, send to the network and cache if possible
                logger?.d(LOG_TAG, "$logPrefix MISS $url")
                val response =  chain.proceed(request)
                if(responseCacheabilityChecker.canStore(response)) {
                    newCacheAndStoreResponse(response, call)
                }else {
                    response
                }
            }
        }

    }
}