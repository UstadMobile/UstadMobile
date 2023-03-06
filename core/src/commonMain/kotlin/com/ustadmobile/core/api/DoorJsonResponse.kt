package com.ustadmobile.core.api

data class DoorJsonResponse(
    val statusCode: Int,
    val contentType: String,
    val headers: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
) {
}