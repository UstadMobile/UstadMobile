package com.ustadmobile.core.impl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.http.UmHttpCall
import com.ustadmobile.core.impl.http.UmHttpRequest
import com.ustadmobile.core.impl.http.UmHttpResponse
import com.ustadmobile.core.impl.http.UmHttpResponseCallback
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.util.UMUUID
import com.ustadmobile.lib.db.entities.HttpCachedEntry

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by mike on 12/26/17.
 *
 * The HttpCache provides transparent synchronous and async http caching, as well as the ability to
 * 'prime' the cache by pre-loading it. It will be expanded to support saving cache entries in multiple
 * directories so that certain items can be 'subscribed' to and thus will not be eligible for deletion.
 *
 * The HttpCache will store etag and last modified information, and (mostly) obey http cache-control
 * and related headers.
 *
 * Placed in com.ustadmobile.core.impl so that the systemimpl makeRequestSync method can be
 * protected and accessed by this class, but it won't be accessible to other classes that
 * shouldn't use it (e.g. presenters).
 *
 * TODO: Answer HTTP head request with information from a cached GET request
 */
class HttpCache(private val sharedDir: String) : HttpCacheResponse.ResponseCompleteListener {

    private val basePrivateDir: String? = null

    private val executorService = Executors.newFixedThreadPool(4)

    private val defaultTimeToLive = DEFAULT_TIME_TO_LIVE

    /**
     * Wrapper HttpCall object that can be used to cancel the request if required. It implements
     * runnable and can be invoked using the executor service.
     */
    private inner class UmHttpCacheCall private constructor(var request: UmHttpRequest, private val responseCallback: UmHttpResponseCallback?//End response callback
    ) : UmHttpCall(), Runnable {

        //Actual http request, if an outgoing http request is actually sent
        private var httpRequest: UmHttpRequest? = null

        //Handler that deals with the http request, if an outgoing request is actually sent
        private var httpResponseHandler: UmHttpResponseHandler? = null

        protected var entry: HttpCachedEntry? = null

        private var async = true

        init {

            async = responseCallback != null
        }

        @Throws(IOException::class)
        fun execute(): UmHttpResponse? {
            val impl = UstadMobileSystemImpl.instance
            var cacheResponse: AbstractCacheResponse? = null

            if (request.url!!.startsWith(PROTOCOL_FILE)) {
                val filePath = UMFileUtil.stripPrefixIfPresent("file://", request.url!!)
                val zipSepPos = filePath.indexOf('!')
                if (zipSepPos == -1) {
                    val responseFile = File(filePath)
                    cacheResponse = FileProtocolCacheResponse(responseFile)
                    if (async) {
                        responseCallback!!.onComplete(this, cacheResponse)
                    }
                } else {
                    cacheResponse = ZipEntryCacheResponse(
                            File(filePath.substring(0, zipSepPos)),
                            filePath.substring(zipSepPos + 1))
                    if (async) {
                        responseCallback!!.onComplete(this, cacheResponse)
                    }
                }

                return cacheResponse
            }

            val entry = getEntry(request.context, request.url, request.getMethod())
            if (entry != null) {
                val timeToLive = if (request.mustRevalidate()) 0 else defaultTimeToLive
                if (isFresh(entry, timeToLive) || request.isOnlyIfCached()) {
                    cacheResponse = HttpCacheResponse(entry, request)
                    cacheResponse.cacheResponse = HttpCacheResponse.HIT_DIRECT
                    UstadMobileSystemImpl.l(UMLog.ERROR, 384, "Cache:HIT_DIRECT: " + request.url!!)

                    //no validation required - directly return the cached response
                    if (async) {
                        responseCallback!!.onComplete(this, cacheResponse)
                    }
                    return cacheResponse

                }
            } else if (request.isOnlyIfCached() && entry == null) {
                val ioe = FileNotFoundException(request.url)
                if (async) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 386,
                            "Cache:onlyIfCached: Fail: not cached: " + request.url!!)
                    responseCallback!!.onFailure(this, ioe)
                    return cacheResponse
                } else {
                    throw ioe
                }
            }

            //make an http request for this cache entry
            httpRequest = UmHttpRequest(request.context!!, request.url!!)
            if (entry != null) {
                if (entry.etag != null) {
                    httpRequest!!.addHeader("if-none-match", entry.etag)
                }
                if (entry.lastModified > 0) {
                    httpRequest!!.addHeader("if-modified-since",
                            UMCalendarUtil.makeHTTPDate(entry.lastModified))
                }
            }

            httpResponseHandler = UmHttpResponseHandler(this)
            if (async) {
                impl.sendRequestAsync(httpRequest!!, httpResponseHandler!!)
            } else {
                httpResponseHandler!!.response = impl.sendRequestSync(httpRequest!!)
                return httpResponseHandler!!.execute()

            }

            return null
        }

        override fun run() {
            try {
                execute()
            } catch (e: IOException) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 73, request.url, e)
            }

        }


        override fun cancel() {
            //TODO: implement cancel
        }
    }

    /**
     * The HttpResponseHandler handles the response from the network after UmHttpCacheCall makes
     * an http call (if needed).
     */
    private inner class UmHttpResponseHandler private constructor(private val cacheCall: UmHttpCacheCall) : Runnable, UmHttpResponseCallback {

        private var response: UmHttpResponse? = null

        override fun onComplete(call: UmHttpCall, response: UmHttpResponse) {
            this.response = response
            executorService.execute(this)
        }

        override fun onFailure(call: UmHttpCall, exception: IOException) {
            cacheCall.responseCallback!!.onFailure(cacheCall, exception)
        }

        override fun run() {
            execute()
        }

        fun execute(): AbstractCacheResponse {
            val responseCacheControlHeader = response!!.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL)

            if (responseCacheControlHeader != null) {
                val responseCacheControl = UMFileUtil.parseParams(responseCacheControlHeader, ',')

                if (responseCacheControl.containsKey(CACHE_CONTROL_NO_CACHE)) {
                    val noCacheResponse = NoCacheResponse(response)
                    if (cacheCall.async) {
                        cacheCall.responseCallback!!.onComplete(cacheCall, noCacheResponse)
                    }
                    return noCacheResponse
                }
            }

            val cacheResponse = cacheResponse(cacheCall.request, response, !cacheCall.async)

            if (cacheCall.async) {
                cacheCall.responseCallback!!.onComplete(cacheCall, cacheResponse)
            }

            return cacheResponse
        }
    }

    private inner class DeleteEntriesTask private constructor(private val context: Any, private val urlsToDelete: Array<String>, private val callback: UmCallback<*>?) : Runnable {

        override fun run() {
            val fileUrisToDelete = UmAppDatabase.getInstance(context).httpCachedEntryDao
                    .findFileUrisByUrl(Arrays.asList(*urlsToDelete))
            val deletedFileUris = ArrayList<String>()
            var entryFile: File
            for (fileUri in fileUrisToDelete) {
                entryFile = File(fileUri)
                if (!entryFile.exists() || entryFile.exists() && entryFile.delete()) {
                    deletedFileUris.add(fileUri)
                } else {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 0, "Failed to deleteByDownloadSetUid cache file: $fileUri")
                }
            }

            UmAppDatabase.getInstance(context).httpCachedEntryDao.deleteByFileUris(deletedFileUris)

            callback?.onSuccess(null)
        }
    }


    init {
        initCache()
    }

    protected fun initCache() {
        val impl = UstadMobileSystemImpl.instance
        val fileIndexIn: InputStream? = null
        //        This class is going to be removed anyway and replaced with using the image library caching mechanisms
        //        try {
        //            if(!new File(sharedDir).exists()) {
        //                impl.makeDirectoryRecursive(sharedDir);
        //            }
        //        }catch(IOException e) {
        //            UstadMobileSystemImpl.l(UMLog.CRITICAL, 4, sharedDir, e);
        //        }finally {
        //            UMIOUtils.closeInputStream(fileIndexIn);
        //        }
    }

    operator fun get(request: UmHttpRequest, callback: UmHttpResponseCallback): UmHttpCall {
        val cacheCall = UmHttpCacheCall(request, callback)
        executorService.execute(cacheCall)
        return cacheCall
    }

    /**
     * Performs an http request synchronously. Returns as soon as the the connection is established.
     * The response will be simultaneously saved to the disk as it's read. If the response comes from
     * the network, this is done using an executor to fork a thread that simultaneously writes to a
     * fileoutputstream to cache the entry to disk and writes to a pipedoutputstream, which connects
     * with a pipedinputstream providing the response to the consumer.
     *
     * The client is expected to read the entire response. Failing to do so could cause an issue as
     * the pipedstream has a limited buffer size.
     *
     * @param request HttpRequest object for the request
     *
     * @return AbstractCacheResponse object representing the http response.
     * @throws IOException If an IOException occurs
     */
    @Throws(IOException::class)
    fun getSync(request: UmHttpRequest): UmHttpResponse? {
        val cacheCall = UmHttpCacheCall(request, null)
        return cacheCall.execute()
    }

    private fun getEntry(context: Any?, url: String?, method: String): HttpCachedEntry? {
        return UmAppDatabase.getInstance(context).httpCachedEntryDao
                .findByUrlAndMethod(url, getMethodFlag(method))
    }

    fun cacheResponse(request: UmHttpRequest, networkResponse: UmHttpResponse,
                      forkSaveToDisk: Boolean): HttpCacheResponse {
        val requestUrl = request.url
        var entry = getEntry(request.context, requestUrl, request.getMethod())
        val responseHasBody = UmHttpRequest.METHOD_HEAD != request.getMethod() && networkResponse.status != 204

        if (entry == null) {
            entry = HttpCachedEntry()
            entry.url = requestUrl
            if (responseHasBody) {
                entry.fileUri = generateCacheEntryFileName(request, networkResponse, sharedDir)
            }
        }

        val cacheResponse = HttpCacheResponse(entry, request)
        cacheResponse.networkResponse = networkResponse
        cacheResponse.entry.lastChecked = System.currentTimeMillis()
        updateCachedEntryFromNetworkResponse(cacheResponse.entry, networkResponse)


        if (networkResponse.status == 304) {
            updateCacheIndex(cacheResponse)
            cacheResponse.isNetworkResponseNotModified = true
            UstadMobileSystemImpl.l(UMLog.INFO, 387, "Cache:HIT_VALIDATED:" + request.url!!)
        } else if (responseHasBody) {
            cacheResponse.setOnResponseCompleteListener(this)
            UstadMobileSystemImpl.l(UMLog.INFO, 385, "Cache:MISS - storing:" + request.url!!)
            if (forkSaveToDisk) {
                cacheResponse.initPipe()
                executorService.execute(cacheResponse)
            } else {
                cacheResponse.saveNetworkResponseToDiskAndBuffer()
            }
        }

        return cacheResponse
    }


    fun deleteEntries(context: Any, urls: Array<String>, callback: UmCallback<*>) {
        executorService.execute(DeleteEntriesTask(context, urls, callback))
    }

    fun deleteEntriesSync(context: Any, urls: Array<String>) {
        DeleteEntriesTask(context, urls, null).run()
    }

    protected fun updateCacheIndex(response: HttpCacheResponse) {
        if (response.networkResponse!!.status == 304) {
            response.cacheResponse = HttpCacheResponse.HIT_VALIDATED
        }

        response.entry.lastAccessed = System.currentTimeMillis()
        UmAppDatabase.getInstance(response.request.context).httpCachedEntryDao
                .insert(response.entry)
    }

    override fun onResponseComplete(response: HttpCacheResponse) {
        updateCacheIndex(response)
    }


    private fun generateCacheEntryFileName(request: UmHttpRequest, response: UmHttpResponse,
                                           dir: String): String {
        val dirFile = File(dir)
        var entryFile: File
        var filename = UMIOUtils.sanitizeIDForFilename(
                UMFileUtil.getFilename(request.url!!))

        var filenameParts = UMFileUtil.splitFilename(filename)
        val contentType = response.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE)
        if (contentType != null) {
            val expectedExtension = UstadMobileSystemImpl.instance.getExtensionFromMimeType(contentType)
            if (expectedExtension != null && expectedExtension != filenameParts[1]) {
                filenameParts = arrayOf(filename, expectedExtension)
                filename = filenameParts[0] + "." + filenameParts[1]
            }
        }

        entryFile = File(dirFile, filename)
        if (!entryFile.exists()) {
            return entryFile.absolutePath
        }

        //try and get to a unique suffix
        for (i in 0..99) {
            entryFile = File(dir, filenameParts[0] + i + '.'.toString() + filenameParts[1])
            if (!entryFile.exists())
                return entryFile.absolutePath
        }

        return File(dir, UMUUID.randomUUID().toString() + "." + filenameParts[1]).absolutePath
    }

    companion object {

        val DEFAULT_TIME_TO_LIVE = 60 * 60 * 1000

        val PROTOCOL_FILE = "file://"

        val CACHE_CONTROL_KEY_MAX_AGE = "max-age"

        val CACHE_CONTROL_NO_CACHE = "no-cache"

        /**
         * Update the given HttpCachedEntry from the response received over the network. This can't be
         * part of the entry  itself, because the entry is in the database module. This will only updateState
         * the object itself and will NOT persist it to the database
         *
         * @param cachedEntry The cached entry to updateState
         * @param networkResponse The network response just received from the network
         */
        fun updateCachedEntryFromNetworkResponse(cachedEntry: HttpCachedEntry, networkResponse: UmHttpResponse) {
            val headerVal: String?
            if (networkResponse.status != 304) {
                //new entry was downloaded - updateState the length etc.
                headerVal = networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_LENGTH)
                if (headerVal != null) {
                    try {
                        cachedEntry.contentLength = Integer.parseInt(headerVal).toLong()
                    } catch (e: IllegalArgumentException) {
                        UstadMobileSystemImpl.l(UMLog.ERROR, 74, headerVal, e)
                    }

                }

                cachedEntry.statusCode = networkResponse.status
            }

            cachedEntry.cacheControl = networkResponse.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL)
            cachedEntry.contentType = networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE)
            cachedEntry.expiresTime = convertDateHeaderToLong(UmHttpRequest.HEADER_EXPIRES, networkResponse)
            cachedEntry.contentType = networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE)
            cachedEntry.etag = networkResponse.getHeader(UmHttpRequest.HEADER_ETAG)
            cachedEntry.cacheControl = networkResponse.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL)
        }

        /**
         * Determine if the entry is considered fresh.
         *
         * @see .calculateEntryExpirationTime
         * @param timeToLive the time since when this entry was last checked for which the entry will be
         * considered fresh if the cache-control headers and expires headers do not
         * provide this information.
         *
         * @return true if the entry is considered fresh, false otherwise
         */
        @JvmOverloads
        fun isFresh(cachedEntry: HttpCachedEntry, timeToLive: Int = DEFAULT_TIME_TO_LIVE): Boolean {
            val expiryTime = calculateEntryExpirationTime(cachedEntry)
            val timeNow = System.currentTimeMillis()

            return if (expiryTime != -1) {
                expiryTime > timeNow
            } else {
                cachedEntry.lastChecked + timeToLive > timeNow
            }
        }

        /**
         * Calculates when an entry will expire based on it's HTTP headers: specifically
         * the expires header and cache-control header
         *
         * As per :  http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html section
         * 14.9.3 the max-age if present will take precedence over the expires header
         *
         * @return -1 if the expiration time calculated from the headers provided if possible, -1 otherwise
         */
        fun calculateEntryExpirationTime(cachedEntry: HttpCachedEntry): Long {
            val cacheControl = cachedEntry.cacheControl
            if (cacheControl != null) {
                val ccParams = UMFileUtil.parseParams(cacheControl, ',')
                if (ccParams.containsKey(CACHE_CONTROL_KEY_MAX_AGE)) {
                    val maxage = Integer.parseInt(ccParams[CACHE_CONTROL_KEY_MAX_AGE] as String).toLong()
                    return cachedEntry.lastChecked + maxage * 1000
                }
            }

            return if (cachedEntry.expiresTime >= 0) {
                cachedEntry.expiresTime
            } else -1

        }


        private fun convertDateHeaderToLong(headerName: String, response: UmHttpResponse): Long {
            val headerVal = response.getHeader(headerName)
            return if (headerVal != null) {
                try {
                    UMCalendarUtil.parseHTTPDate(headerVal)
                } catch (e: NumberFormatException) {
                    -1L
                }

            } else {
                -1L
            }
        }

        /**
         * Get the METHOD_ integer flag for the given HTTP Method as a string
         *
         * @param methodName The HTTP method as a string e.g. "GET", "HEAD", "POST"
         *
         * @return
         */
        fun getMethodFlag(methodName: String): Int {
            if (methodName.equals(UmHttpRequest.METHOD_GET, ignoreCase = true))
                return HttpCachedEntry.METHOD_GET
            else if (methodName.equals(UmHttpRequest.METHOD_HEAD, ignoreCase = true))
                return HttpCachedEntry.METHOD_HEAD
            else if (methodName.equals(UmHttpRequest.METHOD_POST, ignoreCase = true))
                return HttpCachedEntry.METHOD_POST

            return -1
        }
    }

}
