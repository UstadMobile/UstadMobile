package com.ustadmobile.ihttp.ext

import com.ustadmobile.ihttp.iostreams.asIoStreamSource
import kotlinx.io.RawSource

actual fun ByteArray.asSource(): RawSource {
    return this.asIoStreamSource()
}
