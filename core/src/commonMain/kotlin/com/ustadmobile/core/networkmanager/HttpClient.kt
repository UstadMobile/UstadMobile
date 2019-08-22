package com.ustadmobile.core.networkmanager

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature

/**
 * Get default Http client to avoid creating extra thread pool
 */

private val httpClient = HttpClient(){
    install(JsonFeature)
}

fun defaultHttpClient() = httpClient
