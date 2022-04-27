package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.md5Sum
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import kotlin.test.Test
import org.junit.Before
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.mockito.kotlin.mock
import com.ustadmobile.retriever.io.parseIntegrity
import java.io.File
import java.security.MessageDigest
import org.junit.Assert

class ContainerEntryFileIntegrityUpgradePluginTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI
    private lateinit var endpointScope: EndpointScope

    private lateinit var db: UmAppDatabase

    private lateinit var activeEndpoint: Endpoint

    @Before
    fun setup() {
        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
        }

        val accountManager: UstadAccountManager by di.instance()
        activeEndpoint = accountManager.activeEndpoint
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        val connectivityStatus = ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "NetworkSSID")
        db.connectivityStatusDao.insert(connectivityStatus)
    }

    @Test
    fun givenContainerEntryFilesWithNoIntegrityVal_whenProcessJobCalled_thenShouldCalculateIntegrity() {
        val tmpFiles = (1..3).map { index ->
            tmpFolder.newFile().also {
                this::class.java.getResourceAsStream(
                    "/com/ustadmobile/core/container/testfile$index.png")
                    .writeToFile(it)
            }
        }

        val containerEntryFiles = tmpFiles.map {
            ContainerEntryFile(it.md5Sum.encodeBase64(), it.length(), it.length(), 0,
                systemTimeInMillis()
            ).apply {
                cefPath = it.absolutePath
            }
        }

        db.containerEntryFileDao.insertList(containerEntryFiles)

        val upgradeJob = ContentJob().apply {
            cjUid = runBlocking { db.contentJobDao.insertAsync(this@apply) }
        }

        ContentJobItem().apply {
            cjiPluginId = ContainerEntryFileIntegrityUpgradePlugin.PLUGIN_ID
            cjiJobUid = upgradeJob.cjUid
            cjiStatus = JobStatus.QUEUED
            cjiUid = runBlocking { db.contentJobItemDao.insertJobItem(this@apply) }
        }

        val contentJobAndItem = runBlocking {
            db.contentJobItemDao.findNextItemsInQueue(upgradeJob.cjUid, 1).first()
        }

        val plugin = ContainerEntryFileIntegrityUpgradePlugin(Any(), activeEndpoint, di)
        val tmpFolderUri = tmpFolder.newFolder().toDoorUri()

        val processContext = ContentJobProcessContext(
            DoorUri.parse("http://www.ustadmobile.com/"), tmpFolderUri,
            params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(db), di)

        val result = runBlocking { plugin.processJob(contentJobAndItem, processContext, mock { }) }

        //Find the integrity on each containerentryfile, then validate.

        val containerEntriesInDb = db.containerEntryFileDao.findEntriesByMd5Sums(
            containerEntryFiles.mapNotNull { it.cefMd5 })

        containerEntriesInDb.forEach {
            val (algorithm: String, digest: kotlin.ByteArray) =  parseIntegrity(it.cefIntegrity!!)
            val messageDigest = MessageDigest.getInstance(algorithm)
            val fileDigest = messageDigest.digest(File(it.cefPath!!).readBytes())
            Assert.assertArrayEquals("Digest and integrity match for ${it.cefPath}",
                fileDigest, digest)
        }
    }

}