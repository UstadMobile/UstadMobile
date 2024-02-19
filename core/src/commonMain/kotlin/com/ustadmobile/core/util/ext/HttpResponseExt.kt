package com.ustadmobile.core.util.ext

import io.ktor.client.statement.HttpResponse

/**
 * Like bodyAsText, but will uncompress the response using the content-encoding header if present
 */
expect suspend fun HttpResponse.bodyAsDecodedText(): String
