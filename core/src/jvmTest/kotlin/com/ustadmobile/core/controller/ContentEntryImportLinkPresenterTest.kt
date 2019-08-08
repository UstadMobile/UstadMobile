package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.util.test.AbstractImportLinkTest
import com.ustadmobile.util.test.checkJndiSetup
import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*


class ContentEntryImportLinkPresenterTest : AbstractImportLinkTest() {

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

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
            db = UmAppDatabase.getInstance(context)
            repo = db//.getRepository("http://localhost/dummy/", "")
            db.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        server = createServer(db, counter)
        createDb(db)

        val args = Hashtable<String, String>()
        args.put(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-100).toString())
        presenter = ContentEntryImportLinkPresenter(context,
                args, mockView, "http://localhost:8096")
        presenter.onCreate(args)


    }

    var count = 0
    val counter = { value: String, uid: Long, content: String ->
        count++
        Unit
    }


    @After
    fun after() {
        mockWebServer.shutdown()
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

    @Test
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

    @Test
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

            Assert.assertTrue(db.contentEntryParentChildJoinDao.findListOfParentsByChildUuid(-101).isNotEmpty())
            Assert.assertEquals("Func for h5p download called", 1, count)


        }


    }


}