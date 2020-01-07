package com.ustadmobile.lib.contentscrapers.util

import com.ustadmobile.core.container.ContainerManagerCommon
import org.apache.commons.codec.digest.DigestUtils
import java.io.InputStream

class StringEntrySource(private val value: String, override val pathsInContainer: List<String>) : ContainerManagerCommon.EntrySource {

    override val length: Long
        get() = value.length.toLong()

    override val inputStream: InputStream by lazy {
        value.byteInputStream()
    }

    override val filePath: String?
        get() = null

    override val md5Sum: ByteArray by lazy {
        DigestUtils.md5(value)
    }
    override val compression: Int
        get() = 0

    override fun dispose() {

    }
}