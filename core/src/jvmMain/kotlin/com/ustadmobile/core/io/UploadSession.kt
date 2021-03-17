package com.ustadmobile.core.io

import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.readAndSaveToDir
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherJobHttpUrlConnection2
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import kotlinx.coroutines.*
import org.kodein.di.*
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * This class manages a resumable upload session. It will be held in memory until there is an
 * activity timeout. It is designed to receive uploads chunk by chunk. The server receiving
 * requests (e.g. via PUT requests) should call onReceiveChunk. onReceiveChunk MUST be called in
 * order.
 *
 * @param sessionUuid UUID for the session. This is used to create a temporary directory
 * @param containerEntryPaths a list of the paths that should be created in this container. When each
 * ConcatenatedEntry is received, ContainerEntry(s) will be inserted to link to the given container
 * paths.
 * @param md5sExpected the expected order in which md5s will be received (Base64 md5sum)
 * @param siteUrl Endpoint Site URL (used for retrieving dependencies)
 * @param di the dependency injection object
 */
class UploadSession(val sessionUuid: String,
                    val containerEntryPaths: List<ContainerEntryWithMd5>,
                    val md5sExpected: List<String>,
                    val siteUrl: String,
                    override val di: DI) : DIAware, Closeable {

    private val siteEndpoint = Endpoint(siteUrl)

    private val containerDir: File by di.on(siteEndpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val uploadWorkDir: File by lazy {
        File(containerDir, sessionUuid).apply {
            if(!exists())
                mkdirs()
        }
    }

    private val db : UmAppDatabase by di.on(siteEndpoint).instance(tag = DoorTag.TAG_DB)

    val firstFile: File by lazy {
        File(uploadWorkDir, "${md5sExpected.first().base64EncodedToHexString()}${ContainerFetcherJobHttpUrlConnection2.SUFFIX_PART}")
    }

    val firstFileHeader: File by lazy {
        File(uploadWorkDir, "${md5sExpected.first().base64EncodedToHexString()}${ContainerFetcherJobHttpUrlConnection2.SUFFIX_HEADER}")
    }

    val startFromByte: Long by lazy {
        if(firstFile.exists() && firstFileHeader.exists())
            firstFile.length() + firstFileHeader.length()
        else
            0L
    }

    private val pipeOut = PipedOutputStream()

    private val pipeIn = PipedInputStream(pipeOut)

    private val readJob = GlobalScope.launch(Dispatchers.IO) {
        var concatIn: ConcatenatedInputStream2? = null
        try {
            concatIn = ConcatenatedInputStream2(pipeIn)
            concatIn.readAndSaveToDir(containerDir, uploadWorkDir, db, AtomicLong(0L),
                containerEntryPaths, md5sExpected.toMutableList(), "UploadSession")
        }catch(e: Exception) {
            Napier.e("UploadSession;Exception reading, closing pipeOut to ")
            pipeOut.close()
            e.printStackTrace()
        }finally {
            concatIn?.close()
        }
    }

    init {
        UUID.fromString(sessionUuid) //validate this is a real uuid, does not contain nasty characters

        if(startFromByte > 0) {
            FileInputStream(firstFileHeader).use { firstFileHeaderIn ->
                firstFileHeaderIn.copyTo(pipeOut)
            }

            FileInputStream(firstFile).use { firstFileIn ->
                firstFileIn.copyTo(pipeOut)
            }
        }
    }

    /**
     *
     */
    fun onReceiveChunk(chunkInput: InputStream){
        chunkInput.copyTo(pipeOut)
    }

    override fun close() {
        pipeOut.close()
        runBlocking {
            readJob.join()
        }
    }

}