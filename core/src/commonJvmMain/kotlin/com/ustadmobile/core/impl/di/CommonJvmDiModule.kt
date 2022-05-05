package com.ustadmobile.core.impl.di
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.kodein.di.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*


val commonJvmDiModule = DI.Module("CommonJvm") {
    bind<OkHttpClient>() with singleton {
        OkHttpClient.Builder()
            .dispatcher(Dispatcher().also {
                it.maxRequests = 30
                it.maxRequestsPerHost = 10
            })
            .build()
    }

    bind<HttpClient>() with singleton {
        HttpClient(OkHttp) {

            install(JsonFeature) {
                serializer = GsonSerializer()
            }
            install(HttpTimeout)

            val dispatcher = Dispatcher()
            dispatcher.maxRequests = 30
            dispatcher.maxRequestsPerHost = 10

            engine {
                preconfigured = instance()
            }

        }
    }



}