package com.ustadmobile.core.util.ext

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

actual suspend fun HttpResponse.bodyAsDecodedText(): String {
    return bodyAsText()
}
