package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class PhetLinkPluginTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI
    private lateinit var endpointScope: EndpointScope

    private lateinit var mockWebServer: MockWebServer

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private val endpointUrl = "http://localhost/"

    private lateinit var tempUri: DoorUri

    @Before
    fun setup() {
        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
        }

        val accountManager: UstadAccountManager by di.instance()
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        tempUri = tmpFolder.newFolder().toDoorUri()

    }

    @Test
    fun givenValidPhetLink_whenExtractMetaDataCalled_thenShouldParse() {
        val phetPlugin = PhetLinkPlugin(Any(), Endpoint(endpointUrl), di)

        val phetUri = DoorUri.parse("https://phet.colorado.edu/en/simulations/geometric-optics")
        val processContext = ContentJobProcessContext(phetUri, tempUri, params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(db), di)
        runBlocking {
            val result = phetPlugin.extractMetadata(phetUri, processContext)

            //Parima: TODO: Verify that the title and description are loaded. If possible, get the language.
            //            Assert.assertEquals("Geometric Optics", result?.entry?.title)
            Assert.assertEquals("Geometric Optics description", result?.entry?.description)
        }
    }

    @Test
    fun givenLinkIsNotPhet_whenExtractMetaDataCalled_thenShouldReturnNull() {
        val phetPlugin = PhetLinkPlugin(Any(), Endpoint(endpointUrl), di)

        val phetUri = DoorUri.parse("https://google.com/")
        val processContext = ContentJobProcessContext(phetUri, tempUri, params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(db), di)
        runBlocking {
            val result = phetPlugin.extractMetadata(phetUri, processContext)
            Assert.assertEquals(null, result)
        }
    }

    @Test
    fun givenValidPhetLink_whenProcessJobCalled_thenShouldDownloadAndCreateContainer() {
        val containerTmpDir = tmpFolder.newFolder("containerTmpDir")
        val tempFolder = tmpFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())
        val accountManager: UstadAccountManager by di.instance()

        repo = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)
        db = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_DB)


        val doorUri = DoorUri.parse("https://phet.colorado.edu/en/simulations/geometric-optics")
        val processContext = ContentJobProcessContext(doorUri, tempUri, params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(db), di)
        val jobItem = ContentJobItem(sourceUri = doorUri.uri.toString(),
            cjiParentContentEntryUid = 0, cjiContentEntryUid = 42)
        val job = ContentJob(toUri = containerTmpDir.toURI().toString())
        val jobAndItem = ContentJobItemAndContentJob().apply{
            this.contentJob = job
            this.contentJobItem = jobItem
        }

        val phetPlugin = PhetLinkPlugin(Any(), Endpoint(endpointUrl), di)

        runBlocking {
            phetPlugin.processJob(jobAndItem, processContext) {

            }
        }

        val container = runBlocking {
            repo.containerDao.findContainersForContentEntryUid(42).first()
        }


        Assert.assertNotNull(container)

        // Assert Container

        val tinCanEntry = db.containerEntryDao.findByPathInContainer(container.containerUid, "tincan.xml")
        Assert.assertNotNull(tinCanEntry)

    }


}