package com.ustadmobile.ihttp.iostreams

import kotlinx.io.RawSource
import kotlinx.io.asSource
import java.io.ByteArrayInputStream

fun ByteArray.asIoStreamSource(): RawSource {
    return ByteArrayInputStream(this).asSource()
}
