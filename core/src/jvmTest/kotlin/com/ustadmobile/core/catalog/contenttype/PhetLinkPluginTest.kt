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
            Assert.assertEquals("Geometric Optics", result?.entry?.title)
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

}