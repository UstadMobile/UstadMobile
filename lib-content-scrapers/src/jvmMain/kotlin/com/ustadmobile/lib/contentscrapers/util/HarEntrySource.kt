package com.ustadmobile.lib.contentscrapers.util

import com.ustadmobile.core.container.ContainerManagerCommon
import kotlinx.io.InputStream
import kotlinx.serialization.InternalSerializationApi
import net.lightbody.bmp.core.har.HarEntry
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

@ExperimentalStdlibApi
class HarEntrySource(private val harEntry: HarEntry, override val pathsInContainer: List<String>) : ContainerManagerCommon.EntrySource {

    override val length: Long
        get() = harEntry.response.content.size

    override val inputStream: java.io.InputStream
    get() {
        return when {
            harEntry.response.content.encoding == "base64" -> {
                Base64.getDecoder().decode(harEntry.response.content.text).inputStream()

            }
            else -> harEntry.response.content.text.byteInputStream()
        }
    }

    override val filePath: String?
        get() = null

    override val md5Sum: ByteArray by lazy {
        DigestUtils.md5(inputStream)
    }
    override val compression: Int
        get() = 0

    override fun dispose() {

    }
}