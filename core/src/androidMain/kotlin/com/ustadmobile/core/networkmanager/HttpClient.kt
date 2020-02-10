package com.ustadmobile.core.networkmanager

import android.content.Context
import android.os.Build
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

private val OK_HTTP_MIN_SDKVERSION = 21

private val okHttpClient = if(Build.VERSION.SDK_INT > OK_HTTP_MIN_SDKVERSION) {
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

private val httpClient = if(Build.VERSION.SDK_INT < 21) {
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

fun defaultOkHttpClient() = if(Build.VERSION.SDK_INT >= OK_HTTP_MIN_SDKVERSION) {
    okHttpClient!!
} else {
    throw RuntimeException("OKHttp Min SDK Version is $OK_HTTP_MIN_SDKVERSION")
}

fun defaultGsonSerializer() = defaultGsonSerializer

fun initPicasso(context: Context) {
    /**
     * OKHttp does not work on any version of Android less than 5.0 .
     */
    val downloader = if(Build.VERSION.SDK_INT >= 21) {
        OkHttp3Downloader(okHttpClient)
    }else {
        PicassoUrlConnectionDownloader()
    }

    Picasso.setSingletonInstance(Picasso.Builder(context.applicationContext)
            .downloader(downloader)
            .build())
}
