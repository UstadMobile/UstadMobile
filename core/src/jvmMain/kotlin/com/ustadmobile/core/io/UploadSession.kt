package com.ustadmobile.core.io

import io.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.readAndSaveToDir
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherOkHttp
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.core.util.ext.distinctMds5sSorted
import com.ustadmobile.core.util.ext.linkExistingContainerEntries
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import kotlinx.coroutines.*
import org.kodein.di.*
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicLong


/**
 * This class manages a resumable upload session on the server side. It will be held in memory until
 * it is finished or there is an activity timeout. It is designed to receive uploads chunk by chunk.
 *
 * The server receiving requests (e.g. via PUT requests) should call onReceiveChunk when it receives
 * a chunk of data using a post or put request. onReceiveChunk MUST be called in order.
 *
 * When an UploadSession is created, it will check for a previous session with the same UUID. If this
 * is found, any remaining partial data from the previous session will be used. It will also check
 * to see what MD5s the server already has. The client can then use entriesRequired to determine
 * which md5s to upload.
 *
 *
 * @param sessionUuid UUID for the session. This is used to create a temporary directory
 * @param containerEntryPaths a list of the paths that should be created in this container. When each
 * ConcatenatedEntry is received, ContainerEntry(s) will be inserted to link to the given container
 * paths.
 * @param siteUrl Endpoint Site URL (used for retrieving dependencies)
 * @param di the dependency injection object
 */
class UploadSession(val sessionUuid: String,
                    val containerEntryPaths: List<ContainerEntryWithMd5>,
                    val siteUrl: String,
                    override val di: DI) : DIAware, Closeable {

    val lastActive = AtomicLong(systemTimeInMillis())

    private val siteEndpoint = Endpoint(siteUrl)

    private val containerDir: File by di.on(siteEndpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val uploadWorkDir: File by lazy {
        File(containerDir, "upload-$sessionUuid").apply {
            if(!exists())
                mkdirs()
        }
    }

    private val db : UmAppDatabase by di.on(siteEndpoint).instance(tag = DoorTag.TAG_DB)

    /**
     * These are the entries that the client must actually upload.
     */
    private val entriesRequired: List<ContainerEntryWithMd5>

    /**
     * The session parameters that can be given to the client. They specify the starting position
     * for upload and the md5s that actually need uploaded.
     */
    val uploadSessionParams: UploadSessionParams

    private val md5sExpected: List<String>

    private val firstFile: File

    private val firstFileHeader: File

    val startFromByte: Long

    private val pipeOut = PipedOutputStream()

    private val pipeIn = PipedInputStream(pipeOut)

    private val readJob: Job

    init {
        UUID.fromString(sessionUuid) //validate this is a real uuid, does not contain nasty characters

        val containerUid = containerEntryPaths.first().ceContainerUid
        entriesRequired = runBlocking {
            db.linkExistingContainerEntries(containerUid, containerEntryPaths).entriesWithoutMatchingFile
        }

        md5sExpected = entriesRequired.distinctMds5sSorted()

        firstFile = File(uploadWorkDir,
                "${md5sExpected.first().base64EncodedToHexString()}${ContainerFetcherOkHttp.SUFFIX_PART}")
        firstFileHeader = File(uploadWorkDir,
                "${md5sExpected.first().base64EncodedToHexString()}${ContainerFetcherOkHttp.SUFFIX_HEADER}")

        startFromByte = if(firstFile.exists() && firstFileHeader.exists())
            firstFile.length() + firstFileHeader.length()
        else
            0L

        uploadSessionParams = UploadSessionParams(md5sExpected, startFromByte)

        readJob = GlobalScope.launch(Dispatchers.IO) {
            var concatIn: ConcatenatedInputStream2? = null
            try {
                //TODO: this call to readAndSaveToDir must be updated as it no longer links entries etc.
                concatIn = ConcatenatedInputStream2(pipeIn)
                concatIn.readAndSaveToDir(containerDir, uploadWorkDir, db, AtomicLong(0L),
                    md5sExpected.toMutableList(), "UploadSession")
            }catch(e: Exception) {
                Napier.e("UploadSession;Exception reading, closing pipeOut to ")
                pipeOut.close()
                e.printStackTrace()
            }finally {
                concatIn?.close()
            }
        }

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
     * This function is called by the server when an http post or put request containing a chunk of
     * data is received. It MUST be called in order (e.g. the client must send one chunk at a time).
     *
     * @param chunkInput InputStream containing data received by the server
     */
    fun onReceiveChunk(chunkInput: InputStream){
        chunkInput.copyTo(pipeOut)
        pipeOut.flush()
        lastActive.set(systemTimeInMillis())
    }

    override fun close() {
        pipeOut.close()
        runBlocking {
            readJob.join()
            db.linkExistingContainerEntries(containerEntryPaths.first().ceContainerUid,
                entriesRequired)
        }

        uploadWorkDir.takeIf { it.listFiles().isEmpty() }?.delete()
    }

}