package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Status
import okhttp3.*
import okio.BufferedSink
import okio.Okio
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FetchTag(val requestId: Int,
               var stopStatus: Status? = null,
               val stopListeners: MutableList<() -> Unit> = CopyOnWriteArrayList())

class FetchMppJvmImpl(baseOkHttpClient: OkHttpClient): FetchMpp {

    val progessListener: ProgressListener = {requestId: Int, btyesDownloaded: Long, totalBytes: Long ->
        val download = downloadsMap[requestId]
        if(download != null) {
            listeners.forEach { it.onProgress(download, -1, -1) }
        }
    }

    private val downloadsMap: MutableMap<Int, DownloadMppJvmImpl> = ConcurrentHashMap()

    private val okHttpClient: OkHttpClient

    val statusLock = ReentrantLock()

    init {
        okHttpClient = baseOkHttpClient.newBuilder()
                .addInterceptor {chain ->
                    val originalResponse = chain.proceed(chain.request())
                    val fetchTag = chain.request().tag(FetchTag::class.java)
                    val responseBody = originalResponse.body()
                    if(fetchTag != null && responseBody != null) {
                        downloadsMap[fetchTag.requestId]?.status = Status.DOWNLOADING
                        originalResponse.newBuilder().body(ProgressResponseBody(fetchTag.requestId,
                                responseBody, progessListener))
                                .build()
                    }else {
                        originalResponse
                    }
                }
                .build()
    }



    //TODO: See https://android.googlesource.com/platform/external/okhttp/+/a2cab72/samples/guide/src/main/java/com/squareup/okhttp/recipes/Progress.java

    private val listeners = CopyOnWriteArrayList<FetchListenerMpp>()

    override fun enqueue(request: RequestMpp, func: FuncMpp<RequestMpp>?, func2: FuncMpp<Error>?): FetchMpp {
        val okHttpRequest: Request
        try {
            okHttpRequest = Request.Builder().url(request.url)
                    .tag(FetchTag(request.id))
                    .build()
            val downloadMpp = DownloadMppJvmImpl(request.id)
            okHttpClient.newCall(okHttpRequest).also {
                downloadMpp.okHttpCall = it
            }
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    statusLock.withLock {
                        downloadMpp.status = Status.FAILED
                    }

                    listeners.forEach {
                        it.onError(downloadMpp, Error.UNKNOWN_IO_ERROR, e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val sink = Okio.buffer(Okio.sink(File(request.file)))
                    val responseBody = response.body()

                    if(responseBody!== null) {
                        val source = responseBody.source()

                        var bytesWritten = 0L
                        while(source.read(sink.buffer(), 8192).also { bytesWritten += it } != -1L
                                && !call.isCanceled) {
                            sink.emitCompleteSegments()
                        }

                        sink.flush()
                        sink.close()
                        downloadMpp.downloaded = bytesWritten

                        var newStatus: Status? = null
                        var isStopped = false
                        val fetchTag = downloadMpp.okHttpCall?.request()?.tag(FetchTag::class.java)
                        val stopListenersToNotify = mutableListOf<() -> Unit>()
                        statusLock.withLock {
                            if(!call.isCanceled) {
                                newStatus = Status.COMPLETED
                            }else {
                                isStopped = true
                                newStatus = Status.PAUSED
                                if(fetchTag != null) {
                                    stopListenersToNotify.addAll(fetchTag.stopListeners)
                                    fetchTag.stopListeners.clear()
                                }
                            }

                            val newStatusVal = newStatus
                            if(newStatusVal != null) {
                                downloadMpp.status = newStatusVal
                            }
                        }

                        stopListenersToNotify.forEach { it.invoke() }

                        //these are the only events we are looking at - therefor non-exhaustive is OK
                        @Suppress("NON_EXHAUSTIVE_WHEN")
                        when(newStatus) {
                            Status.COMPLETED -> listeners.forEach { it.onCompleted(downloadMpp) }
                            Status.PAUSED -> listeners.forEach { it.onPaused(downloadMpp) }
                        }
                    }else {
                        listeners.forEach {
                            it.onError(downloadMpp, Error.UNKNOWN_IO_ERROR,
                                    IOException("Null response body"))
                        }
                    }
                }
            })
            downloadMpp.status = Status.QUEUED
            func?.call(request)
        }catch(e: Exception) {
            func2?.call(Error.UNKNOWN)
        }

        return this
    }



    override fun pause(id: Int, func: FuncMpp<DownloadMpp>?, func2: FuncMpp<Error>?): FetchMpp {
        val downloadMpp = downloadsMap[id]
        val okHttpCall = downloadMpp?.okHttpCall
        statusLock.withLock {
            if(downloadMpp != null && okHttpCall != null
                    && downloadMpp.status in listOf(Status.DOWNLOADING, Status.QUEUED)) {
                val fetchTag = okHttpCall.request().tag(FetchTag::class.java)
                fetchTag?.stopStatus = Status.PAUSED
                fetchTag?.stopListeners?.add { func?.call(downloadMpp) }
                okHttpCall.cancel()
            }else {
                func2?.call(Error.DOWNLOAD_NOT_FOUND)
            }
        }

        return this
    }

    override fun resume(id: Int, func: FuncMpp<DownloadMpp>?, func2: FuncMpp<Error>?): FetchMpp {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enqueue(requests: List<RequestMpp>, func: FuncMpp<List<Pair<RequestMpp, Error>>>?): FetchMpp {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addListener(listener: FetchListenerMpp): FetchMpp {
        listeners.add(listener)
        return this
    }

    override fun removeListener(listener: FetchListenerMpp): FetchMpp {
        listeners.remove(listener)
        return this
    }
}