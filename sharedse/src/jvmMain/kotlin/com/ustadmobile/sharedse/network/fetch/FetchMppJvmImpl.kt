package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error
import okhttp3.*
import okio.BufferedSink
import okio.Okio
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class FetchTag(val requestId: Int)

class FetchMppJvmImpl(baseOkHttpClient: OkHttpClient): FetchMpp {

    val progessListener: ProgressListener = {requestId: Int, btyesDownloaded: Long, totalBytes: Long ->
        val download = downloadsMap[requestId]
        if(download != null) {
            listeners.forEach { it.onProgress(download, -1, -1) }
        }
    }

    private val downloadsMap: MutableMap<Int, DownloadMpp> = ConcurrentHashMap()

    private val okHttpClient: OkHttpClient

    init {
        okHttpClient = baseOkHttpClient.newBuilder()
                .addInterceptor {chain ->
                    val originalResponse = chain.proceed(chain.request())
                    val fetchTag = chain.request().tag(FetchTag::class.java)
                    val responseBody = originalResponse.body()
                    if(fetchTag != null && responseBody != null) {
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
                    listeners.forEach {
                        it.onError(downloadMpp, Error.UNKNOWN_IO_ERROR, e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val sink = Okio.buffer(Okio.sink(File(request.file)))
                    val responseBody = response.body()
                    if(responseBody!== null) {
                        sink.writeAll(responseBody.source())
                        sink.flush()
                        sink.close()
                        downloadMpp.downloaded = responseBody.contentLength()
                        listeners.forEach {
                            it.onCompleted(downloadMpp)
                        }
                    }else {
                        func2?.call(Error.UNKNOWN)
                    }
                }
            })
            func?.call(request)
        }catch(e: Exception) {
            func2?.call(Error.UNKNOWN)
        }

        return this
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