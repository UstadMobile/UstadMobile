package com.ustadmobile.core.networkmanager

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

/**
 * Note: OKHttp Ktor default configuration since 1.3.1 could cause a problem:
 * See "Sporadic OkHttp errors after upgrading to ktor 1.3.1"
 *  https://github.com/ktorio/ktor/issues/1708
 */

private val OK_HTTP_MIN_SDKVERSION = 50

private val okHttpClient = if(Build.VERSION.SDK_INT >= OK_HTTP_MIN_SDKVERSION) {
    OkHttpClient.Builder()
            .dispatcher(Dispatcher().also {
                it.maxRequests = 30
                it.maxRequestsPerHost = 10
            })
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()
}else {
    null
}

private val defaultGsonSerializer = GsonSerializer()

private val defaultGson: Gson by lazy {Gson()}

fun defaultGson() = defaultGson

private val httpClient = if(Build.VERSION.SDK_INT < OK_HTTP_MIN_SDKVERSION) {
    HttpClient(Android) {
        install(JsonFeature)
    }
}else {
    HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = defaultGsonSerializer
        }

        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 30
        dispatcher.maxRequestsPerHost = 10

        engine {
            preconfigured = okHttpClient!!
        }

    }
}

actual fun defaultHttpClient() = httpClient

fun initPicasso(context: Context) {
    /**
     * OKHttp does not work on any version of Android less than 5.0 .
     */
    val downloader = if(Build.VERSION.SDK_INT >= OK_HTTP_MIN_SDKVERSION) {
        OkHttp3Downloader(okHttpClient)
    }else {
        PicassoUrlConnectionDownloader()
    }

    Picasso.setSingletonInstance(Picasso.Builder(context.applicationContext)
            .downloader(downloader)
            .build())
}
