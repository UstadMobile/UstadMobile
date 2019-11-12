package com.ustadmobile.core.networkmanager

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import okhttp3.Dispatcher
import java.util.concurrent.TimeUnit

private val httpClient = if(Build.VERSION.SDK_INT < 21) {
    HttpClient(CIO) {
        install(JsonFeature)
    }
}else {
    HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }

        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 30
        dispatcher.maxRequestsPerHost = 10

        engine {
            this.config {
                dispatcher(dispatcher)
                connectTimeout(45, TimeUnit.SECONDS)
                readTimeout(45, TimeUnit.SECONDS)
            }
        }

    }
}

actual fun defaultHttpClient() = httpClient
