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
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import com.ustadmobile.util.test.ext.baseDebugIfNotEnabled
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
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

    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Test
    fun givenFileUploadedInChunks_whenClosed_thenCompletedContainerShouldBeSaved() {
        val entriesToUpload = clientDb.containerEntryDao.findByContainer(container.containerUid)

        val md5sToWrite = entriesToUpload.mapNotNull { it.containerEntryFile?.cefMd5?.base64EncodedToHexString() }

        val uploadToWrite = clientDb.containerEntryFileDao.generateConcatenatedFilesResponse2(
                md5sToWrite.joinToString(separator = ";"), mapOf(), clientDb)


        val uploadSession = UploadSession(UUID.randomUUID().toString(),
                entriesToUpload.map { it.toContainerEntryWithMd5() }, md5sToWrite,
                serverEndpoint.url, null, di)

        val pipeIn = PipedInputStream()
        val pipeOut = PipedOutputStream(pipeIn)

        GlobalScope.launch {
            uploadToWrite.writeTo(pipeOut)
            pipeOut.close()
        }

        val uploadBuffer = ByteArray(200 * 1024)//200K chunks
        var bytesRead = 0
        while(pipeIn.read(uploadBuffer).also { bytesRead = it } != -1) {
            uploadSession.onReceiveChunk(ByteArrayInputStream(uploadBuffer, 0, bytesRead))
        }

        uploadSession.close()

        clientDb.assertContainerEqualToOther(container.containerUid, serverDb)
    }


}