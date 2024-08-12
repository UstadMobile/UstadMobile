package com.ustadmobile.ihttp.ext

import kotlinx.io.Buffer
import kotlinx.io.RawSource

/**
 * As per
 * https://github.com/Kotlin/kotlinx-io/blob/master/core/common/test/CommonRealSourceTest.kt
 */
actual fun ByteArray.asSource(): RawSource {
    val buffer = Buffer().also {
        it.write(this)
    }

    val src = object: RawSource by buffer {
        override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
            return buffer.readAtMostTo(sink, minOf(1, byteCount))
        }
    }

    return src
}