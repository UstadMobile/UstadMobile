package com.ustadmobile.core.io

import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.io.ext.generateConcatenatedFilesResponse2
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.core.util.ext.linkExistingContainerEntries
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import com.ustadmobile.util.test.ext.baseDebugIfNotEnabled
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.*
import java.util.*

class UploadSessionTest {

    @Rule
    @JvmField
    val ustadTestRule = UstadTestRule()

    lateinit var di: DI

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    /**
     * This is used so that we can get a completely different database using the same di. The test
     * will use the client endpoint scope to get the data to be written
     */
    val clientEndpoint = Endpoint("http://client.endpoint/")

    lateinit var serverEndpoint: Endpoint

    lateinit var clientDb: UmAppDatabase

    lateinit var clientRepo: UmAppDatabase

    lateinit var serverDb: UmAppDatabase

    lateinit var container: Container

    lateinit var entriesToUpload: List<ContainerEntryWithMd5>

    @Suppress("BlockingMethodInNonBlockingContext")
    @Before
    fun setup() {
        Napier.baseDebugIfNotEnabled()
        di = DI {
            import(ustadTestRule.diModule)
            bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(ustadTestRule.endpointScope).singleton {
                temporaryFolder.newFolder()
            }
        }

        val siteUrl: String = di.direct.instance<UstadAccountManager>().activeAccount.endpointUrl

        serverEndpoint = Endpoint(siteUrl)

        clientDb = di.direct.on(clientEndpoint).instance(tag = DoorTag.TAG_DB)
        clientRepo = di.direct.on(clientEndpoint).instance(tag = DoorTag.TAG_REPO)

        serverDb = di.direct.on(serverEndpoint).instance(tag = DoorTag.TAG_DB)

        container = Container().apply {
            containerUid = clientRepo.containerDao.insert(this)
        }

        runBlocking {
            clientRepo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
                    "/com/ustadmobile/core/contentformats/epub/test.epub",
                    ContainerAddOptions(storageDirUri = temporaryFolder.newFolder().toDoorUri()))
        }

        entriesToUpload = clientDb.containerEntryDao.findByContainer(container.containerUid)
                .map { it.toContainerEntryWithMd5() }


    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Test
    fun givenFileUploadedInChunks_whenClosed_thenCompletedContainerShouldBeSaved() {
        val md5sToWrite = entriesToUpload.mapNotNull { it.cefMd5?.base64EncodedToHexString() }
                .sorted()
        val uploadToWrite = clientDb.containerEntryFileDao.generateConcatenatedFilesResponse2(
                md5sToWrite.joinToString(separator = ";"), mapOf(), clientDb)
        
        val uploadSession = UploadSession(UUID.randomUUID().toString(),
                entriesToUpload, md5sToWrite,
                serverEndpoint.url, null, di)

        val byteArrayOut = ByteArrayOutputStream().also {
            uploadToWrite.writeTo(it)
            it.flush()
        }

        val uploadBuffer = ByteArray(200 * 1024)//200K chunks
        var bytesRead = 0
        val byteArrayIn = ByteArrayInputStream(byteArrayOut.toByteArray())
        while(byteArrayIn.read(uploadBuffer).also { bytesRead = it } != -1) {
            uploadSession.onReceiveChunk(ByteArrayInputStream(uploadBuffer, 0, bytesRead))
        }

        uploadSession.close()

        clientDb.assertContainerEqualToOther(container.containerUid, serverDb)
    }

    @Test
    fun givenFileUploadedInTwoSessions_whenClosed_thenCompletedContainerShouldBeSaved() {
        var totalBytesRead = 0

        val uploadSessionUuid = UUID.randomUUID()

        var lastStartFrom = 0L

        for(i in 0 .. 1) {
            //figure out what remains
            val containerEntriesPartition = runBlocking {
                serverDb.linkExistingContainerEntries(container.containerUid,
                        entriesToUpload)
            }

            val remainingEntries = containerEntriesPartition.entriesWithoutMatchingFile
                    .sortedBy { it.cefMd5 }
            val md5sToWrite = remainingEntries.mapNotNull { it.cefMd5?.base64EncodedToHexString() }



            val uploadSession1 = UploadSession(uploadSessionUuid.toString(),
                    remainingEntries, md5sToWrite, serverEndpoint.url, null, di)

            val uploadToWrite1 = clientDb.containerEntryFileDao.generateConcatenatedFilesResponse2(
                    md5sToWrite.joinToString(separator = ";"),
                    mapOf("range" to listOf("bytes=${uploadSession1.startFromByte}-")),
                    clientDb)

            lastStartFrom = uploadSession1.startFromByte

            val byteArrayOut = ByteArrayOutputStream().also {
                uploadToWrite1.writeTo(it)
                it.flush()
            }

            val uploadBuffer = ByteArray(200 * 1024)//200K chunks
            var bytesRead = 0
            val byteArrayIn = ByteArrayInputStream(byteArrayOut.toByteArray())
            while(byteArrayIn.read(uploadBuffer).also { bytesRead = it } != -1
                    && !(i == 0 && totalBytesRead > (uploadToWrite1.actualContentLength / 2))) {
                uploadSession1.onReceiveChunk(ByteArrayInputStream(uploadBuffer, 0, bytesRead))
                totalBytesRead += bytesRead
            }

            byteArrayIn.close()
            uploadSession1.close()
        }

        clientDb.assertContainerEqualToOther(container.containerUid, serverDb)
        Assert.assertTrue("Resumed upload from starting position", lastStartFrom > 0)
    }


    @Test
    fun givenCorruptedIntermediateData_whenUploaded_thenShouldDeleteCorruptedPartAndResume() {
        var totalBytesRead = 0

        var corruptPacketWritten = false

        val uploadUuid = UUID.randomUUID()

        for (i in 0..1) {
            //figure out what remains
            val containerEntriesPartition = runBlocking {
                serverDb.linkExistingContainerEntries(container.containerUid,
                        entriesToUpload)
            }

            val remainingEntries = containerEntriesPartition.entriesWithoutMatchingFile
                    .sortedBy { it.cefMd5 }
            val md5sToWrite = remainingEntries.mapNotNull { it.cefMd5?.base64EncodedToHexString() }

            val uploadSession1 = UploadSession(uploadUuid.toString(),
                    remainingEntries, md5sToWrite, serverEndpoint.url, null, di)

            val uploadToWrite1 = clientDb.containerEntryFileDao.generateConcatenatedFilesResponse2(
                    md5sToWrite.joinToString(separator = ";"),
                    mapOf("range" to listOf("bytes=${uploadSession1.startFromByte}-")), clientDb)


            val byteArrayOut = ByteArrayOutputStream().also {
                uploadToWrite1.writeTo(it)
                it.flush()
            }

            val byteArrayIn = ByteArrayInputStream(byteArrayOut.toByteArray())
            try {
                val uploadBuffer = ByteArray(200 * 1024)//200K chunks
                var bytesRead = 0
                while(byteArrayIn.read(uploadBuffer).also { bytesRead = it } != -1) {
                    if(!corruptPacketWritten && i == 0 &&
                            totalBytesRead > (uploadToWrite1.actualContentLength / 2)) {
                        //write a corrupt packet
                        uploadSession1.onReceiveChunk(ByteArrayInputStream(ByteArray(uploadBuffer.size)))
                        corruptPacketWritten = true
                    }else {
                        uploadSession1.onReceiveChunk(ByteArrayInputStream(uploadBuffer, 0, bytesRead))
                    }

                    totalBytesRead += bytesRead
                }

            }catch(e: Exception) {
                e.printStackTrace()
            }finally {
                byteArrayIn.close()
                uploadSession1.close()
            }
        }

        clientDb.assertContainerEqualToOther(container.containerUid, serverDb)
        //Note: When a concatenatedentry is corrupted, it will be deleted, and the next uploadSession
        //starts at zero, hence there is no assertion here of using a range request etc.
    }
}