package com.ustadmobile.ihttp.ext

import kotlinx.io.RawSource
import com.ustadmobile.ihttp.iostreams.asIoStreamSource

actual fun ByteArray.asSource(): RawSource {
    return this.asIoStreamSource()
}
