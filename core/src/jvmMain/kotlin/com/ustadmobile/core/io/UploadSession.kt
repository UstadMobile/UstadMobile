package com.ustadmobile.core.io

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import java.io.*
import java.util.*

/**
 * This class manages a resumable upload session. It will be held in memory until there is an
 * activity timeout.
 */
class UploadSession(val sessionUuid: String,
                    val containerEntryPaths: List<ContainerEntryWithMd5>,
                    val siteUrl: String,
                    val resumeFromMd5: String?,
                    override val di: DI) : DIAware, Closeable {

    private val uploadDir: File by di.on(Endpoint(siteUrl)).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    val pipeOut = PipedOutputStream()

    val pipeIn = PipedInputStream(pipeOut)

    val readJob = GlobalScope.launch(Dispatchers.IO) {
        var concatIn: ConcatenatedInputStream2? = null
        try {
            concatIn = ConcatenatedInputStream2(pipeIn)

            lateinit var concatenatedEntry: ConcatenatedEntry
            while(isActive && concatIn.getNextEntry()?.also { concatenatedEntry = it } != null) {
                val entryMd5 = concatenatedEntry.md5.toHexString()

            }
        }catch(e: Exception) {

        }
    }

    init {
        UUID.fromString(sessionUuid) //validate this is a real uuid, does not contain nasty characters


    }

    fun onReceiveChunk(chunkInput: InputStream, close: Boolean){
        chunkInput.copyTo(pipeOut)


    }

    override fun close() {

    }

    companion object {
        const val SUFFIX_PART = ".part"

        const val SUFFIX_HEADER = ".header"
    }
}