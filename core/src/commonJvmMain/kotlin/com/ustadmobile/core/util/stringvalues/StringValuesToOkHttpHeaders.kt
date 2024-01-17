package com.ustadmobile.core.util.stringvalues

import okhttp3.Headers

fun IStringValues.asOkHttpHeaders(): Headers {
    val headerLines = names().flatMap { name ->
        getAll(name).flatMap { listOf(name, it) }
    }
    return Headers.headersOf(*headerLines.toTypedArray())
}
