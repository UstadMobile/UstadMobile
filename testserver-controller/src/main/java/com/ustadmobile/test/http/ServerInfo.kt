package com.ustadmobile.test.http

import kotlinx.serialization.Serializable

@Serializable
data class ServerInfo(
    val url: String,
    val port: Int,
    val extraInfo: String,
)