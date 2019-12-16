package com.ustadmobile.lib.contentscrapers.util

import com.ustadmobile.core.container.ContainerManagerCommon
import kotlinx.io.InputStream
import net.lightbody.bmp.core.har.HarEntry
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class HarEntrySource(private val harEntry: HarEntry, override val pathInContainer: String) : ContainerManagerCommon.EntrySource {

    override val length: Long
        get() = harEntry.response.content.size

    override val inputStream: InputStream
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
}