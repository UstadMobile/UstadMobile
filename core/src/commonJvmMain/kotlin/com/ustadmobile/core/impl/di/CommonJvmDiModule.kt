package com.ustadmobile.core.impl.di
import com.ustadmobile.core.util.FolderSelector
import com.ustadmobile.core.util.createFolderSelector
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.kodein.di.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json


val CommonJvmDiModule = DI.Module("CommonJvm") {

    bind<FolderSelector>() with singleton { createFolderSelector(context) }

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

            install(ContentNegotiation) {
                json(json = instance())
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