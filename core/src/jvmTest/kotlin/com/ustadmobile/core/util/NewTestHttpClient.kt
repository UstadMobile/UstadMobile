package com.ustadmobile.core.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

fun OkHttpClient.newTestHttpClient(
    json: Json
): HttpClient {
    return HttpClient(OkHttp) {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(json = json)
        }

        engine {
            preconfigured = this@newTestHttpClient
        }
    }
}