package com.ustadmobile.core.api

import io.ktor.http.*

data class DoorJsonRequest(
    val method: Method,
    val url: Url,
    val headers: Map<String, String>,
    val requestBody: String? = null,
){

    enum class Method {
        GET, POST, PUT, DELETE
    }

}