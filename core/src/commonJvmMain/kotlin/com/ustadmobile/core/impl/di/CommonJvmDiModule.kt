package com.ustadmobile.core.impl.di
import com.ustadmobile.core.contentformats.xapi.*
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.kodein.di.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.json.*
import io.ktor.serialization.gson.*


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

            install(ContentNegotiation) {
                gson {
                    registerTypeAdapter(Statement::class.java, StatementSerializer())
                    registerTypeAdapter(Statement::class.java, StatementDeserializer())
                    registerTypeAdapter(ContextActivity::class.java, ContextDeserializer())
                }
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