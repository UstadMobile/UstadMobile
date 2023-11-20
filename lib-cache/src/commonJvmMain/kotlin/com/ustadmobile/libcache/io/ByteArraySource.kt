package com.ustadmobile.libcache.io

import kotlinx.io.RawSource
import kotlinx.io.asSource
import java.io.ByteArrayInputStream

actual fun ByteArray.asKotlinxIoSource(): RawSource {
    return ByteArrayInputStream(this).asSource()
}
