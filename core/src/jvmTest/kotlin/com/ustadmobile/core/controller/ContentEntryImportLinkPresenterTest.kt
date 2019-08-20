package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.util.test.AbstractImportLinkTest
import com.ustadmobile.util.test.checkJndiSetup
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.stopServerOnCancellation
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit


class ContentEntryImportLinkPresenterTest : AbstractImportLinkTest() {

    private lateinit var context: Any

    private lateinit var serverdb: UmAppDatabase

    private lateinit var defaultDb: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private var mockView: ContentEntryImportLinkView = mock {}

    private lateinit var presenter: ContentEntryImportLinkPresenter

    var mockWebServer = MockWebServer()

    private lateinit var server: ApplicationEngine

    @Before
    fun setup() {
        checkJndiSetup()

        context = Any()
        try {
            serverdb = UmAppDatabase.getInstance(context, "serverdb")
            defaultDb = UmAppDatabase.getInstance(context)
            repo = serverdb//.getRepository("http://localhost/dummy/", "")
            serverdb.clearAllTables()
            defaultDb.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        server = createServer(serverdb, counter)
        createDb(serverdb)
        createDb(defaultDb)

        val args = Hashtable<String, String>()
        args[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = (-101).toString()
        presenter = ContentEntryImportLinkPresenter(context,
                args, mockView, "http://localhost:8096")
        presenter.onCreate(args)

    }

    var count = 0
    val counter = { value: String, uid: Long, content: String, containerUid: Long ->
        count++
        Unit
    }


    @After
    fun after() {
        mockWebServer.shutdown()
        server.stop(1, 5, TimeUnit.SECONDS)
    }


    @Test
    fun givenWhenHandleUrlTextUpdated_whenEmptyScreen_showInvalidUrlMessage() {
        runBlocking {
            presenter.handleUrlTextUpdated("hello")
            verify(mockView).showUrlStatus(false, "Invalid Url")
        }


    }

    @Test
    fun givenWhenHandleUrlTextUpdated_whenNotHp5_showUnSupportedContent() {

        mockWebServer.enqueue(MockResponse().setBody("no h5p here").setResponseCode(200))
        mockWebServer.start()

        runBlocking {
            presenter.handleUrlTextUpdated(mockWebServer.url("/nohp5here").toString())

            verify(mockView, timeout(15000)).showUrlStatus(false, "Content not supported")
        }


    }

    //20/Aug/2019: This test needs to be reworked after H5P and video import is live
    //@Test
    fun givenWhenHandleUrlTextUpdated_whenHp5_showValidAndNoMessage() {

        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.start()
        val url = mockWebServer.url("/somehp5here").toString()


        runBlocking {
            presenter.handleUrlTextUpdated(url)

            verify(mockView, timeout(15000)).showUrlStatus(true, "")
            verify(mockView, timeout(15000)).displayUrl(url)

        }

    }

    //20/Aug/2019: This test needs to be reworked after H5P and video import is live
    //@Test
    fun givenUserClicksDone_whenH5PLinkIsValid_thenDownloadContent() {

        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.start()
        val url = mockWebServer.url("/somehp5here").toString()

        runBlocking {

            presenter.handleUrlTextUpdated(url)
            presenter.handleClickImport()

            Assert.assertTrue(defaultDb.contentEntryParentChildJoinDao.findListOfChildsByParentUuid(-101).isNotEmpty())
            Assert.assertTrue(serverdb.contentEntryParentChildJoinDao.findListOfChildsByParentUuid(-101).isNotEmpty())
            Assert.assertEquals("Func for h5p download called", 1, count)


        }


    }


}