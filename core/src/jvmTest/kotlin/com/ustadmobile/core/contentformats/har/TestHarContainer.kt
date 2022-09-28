package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.util.*
import com.ustadmobile.core.view.ReportListView
import com.ustadmobile.door.ext.currentDoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.mockito.kotlin.mock

class TestHarContainer {

    private lateinit var container: Container
    var harContainer: HarContainer? = null

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    @Rule
    @JvmField
    val tmpFileRule = TemporaryFolder()

    private lateinit var mockView: ReportListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var di: DI


    @Before
    fun setup() {

        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.STARTED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()
        val db: UmAppDatabase by di.activeDbInstance()
        val accountManager: UstadAccountManager by di.instance()

        val httpd = EmbeddedHTTPD(0, di)
        httpd.start()

        val tmpDir = tmpFileRule.newFolder("testHar")

        val targetEntry = ContentEntry()
        targetEntry.title = "tiempo de prueba"
        targetEntry.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        targetEntry.description = "todo el contenido"
        targetEntry.publisher = "CK12"
        targetEntry.author = "borrachera"
        targetEntry.primaryLanguageUid = 53
        targetEntry.leaf = true
        targetEntry.contentEntryUid = repo.contentEntryDao.insert(targetEntry)

        container = Container()
        container.mimeType = "application/har+zip"
        container.containerContentEntryUid = targetEntry.contentEntryUid
        container.containerUid = repo.containerDao.insert(container)

        runBlocking {
            repo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
                    "/com/ustadmobile/core/contentformats/har.zip", ContainerAddOptions(tmpDir.toDoorUri()))

            harContainer = HarContainer(container.containerUid, targetEntry,
                    accountManager.activeAccount, db, context, httpd.localHttpUrl, di.direct.instance()){
            }
            harContainer?.startingUrlDeferred?.await()
        }
    }

    @Test
    fun givenRequest_whenServedByContainer_thenSameResponse() {
        runBlocking {
            val response = harContainer?.serve(HarRequest().apply {
                this.url = "http://www.ustadmobile.com/index.html"
                this.body = "index.html"
                this.method = "GET"
            })

            Assert.assertEquals("index html was found", 200, response!!.status)
        }
    }

    @Test
    fun givenUrlLoaded_whenNotInIndex_Return404ErrorResponse() {
        runBlocking {
            val response = harContainer?.serve(HarRequest().apply {
                this.url = "http://www.ustadmobile.com/faketest.html"
                this.body = "faketest.html"
                this.method = "GET"
            })

            Assert.assertEquals("index html was found", 404, response!!.status)
        }

    }

    @Test
    fun givenUrlLoaded_whenInIndexButContainerMissing_thenReturn404ErrorResponse() {
        runBlocking {
            val response = harContainer?.serve(HarRequest().apply {
                this.url = "http://www.ustadmobile.com/favicon.ico"
                this.body = "favicon.ico"
                this.method = "GET"
            })

            Assert.assertEquals("index html was found", 402, response!!.status)
        }
    }

}