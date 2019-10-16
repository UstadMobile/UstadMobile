package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter.Companion.FILE_SIZE
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter.Companion.GOOGLE_DRIVE_LINK
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.util.test.AbstractImportLinkTest
import com.ustadmobile.util.test.checkJndiSetup
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.stopServerOnCancellation
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.toUtf8Bytes
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.Okio
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.*
import java.util.concurrent.TimeUnit


class ContentEntryImportLinkPresenterTest : AbstractImportLinkTest() {

    private lateinit var context: Any

    private lateinit var serverdb: UmAppDatabase

    private lateinit var defaultDb: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var mockView: ContentEntryImportLinkView

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

        mockView = mock {}

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
    fun givenWhenHandleUrlTextUpdated_whenInvalidUrl_showInvalidUrlMessage() {
        presenter.handleUrlTextUpdated("hello")
        verify(mockView, timeout(5000)).showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))
    }

    @Test
    fun givenWhenHandleUrlTextUpdated_whenUrlRespondsWithError_showInvalidUrlMessage() {

        mockWebServer.enqueue(MockResponse().setBody("no h5p here").setResponseCode(404))
        mockWebServer.start()

        presenter.handleUrlTextUpdated(mockWebServer.url("/nohp5here").toString())
        verify(mockView, timeout(5000)).showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))

    }


    @Test
    fun givenWhenHandleUrlTextUpdated_whenNotHp5_showUnSupportedContent() {

        mockWebServer.enqueue(MockResponse().setBody("no h5p here").setResponseCode(200))
        mockWebServer.start()

        presenter.handleUrlTextUpdated(mockWebServer.url("/nohp5here").toString())
        verify(mockView, timeout(5000)).showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_content_not_supported, context))


    }

    @Test
    fun givenWhenHandleUrlTextUpdated_whenInvalidGoogleDriveLink_showUnSupportedContent() {


        mockWebServer.start()

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "video/")
                .setHeader("location", mockWebServer.url("/noVideoHere"))
                .setResponseCode(302))

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "audio/")
                .setResponseCode(200))

        GOOGLE_DRIVE_LINK = mockWebServer.url("/google.drive.com").toString()

        presenter.handleUrlTextUpdated(mockWebServer.url("/google.drive.com").toString())
        verify(mockView, timeout(5000)).showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_content_not_supported, context))


    }


    @Test
    fun givenWhenHandleUrlTextUpdated_whenContentSupportedButNotH5P_showInvalidUrl() {

        mockWebServer.enqueue(MockResponse().setHeader("Content-Type", "text/html; charset=utf-8").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody(""))
        mockWebServer.start()
        val url = mockWebServer.url("/somehp5here").toString()


        presenter.handleUrlTextUpdated(url)
        verify(mockView, timeout(5000)).showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))


    }

    @Test
    fun givenWhenHandleUrlTextUpdated_whenContentSupportedButNull_showInvalidUrl() {

        mockWebServer.enqueue(MockResponse().setHeader("Content-Type", "text/html; charset=utf-8").setResponseCode(200))
        mockWebServer.enqueue(MockResponse())
        mockWebServer.start()
        val url = mockWebServer.url("/somehp5here").toString()


        presenter.handleUrlTextUpdated(url)
        verify(mockView, timeout(5000)).showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))

    }


    @Test
    fun givenWhenHandleUrlTextUpdated_whenContentIsNotH5P_showInvalidUrl() {

        mockWebServer.enqueue(MockResponse().setHeader("Content-Type", "text/html; charset=utf-8").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("no h5p here"))
        mockWebServer.start()
        val url = mockWebServer.url("/somehp5here").toString()

        presenter.handleUrlTextUpdated(url)
        verify(mockView, timeout(5000)).showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))

    }


    @Test
    fun givenWhenHandleUrlTextUpdated_whenVideoSizeIsTooBig_showErrorMessageWithFileSize() {

        mockWebServer.enqueue(MockResponse().setHeader("content-length", FILE_SIZE).setHeader("Content-Type", "video/").setResponseCode(200))
        mockWebServer.start()

        presenter.handleUrlTextUpdated(mockWebServer.url("/nohp5here").toString())
        verify(mockView, timeout(5000)).showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_big_size, context))
    }


    @Test
    fun givenWhenHandleUrlTextUpdated_whenValidVideo_showEditTitleToUser() {

        mockWebServer.enqueue(MockResponse().setHeader("content-length", 11).setHeader("content-type", "video/").setResponseCode(200))
        mockWebServer.start()

        presenter.handleUrlTextUpdated(mockWebServer.url("/nohp5here").toString())
        verify(mockView, timeout(5000)).showUrlStatus(true, "")
        verify(mockView, timeout(5000)).showHideVideoTitle(true)
    }


    @Test
    fun givenWhenHandleUrlTextUpdated_whenHp5_showValidAndNoMessage() {

        mockWebServer.enqueue(MockResponse().setHeader("content-type", "text/html; charset=utf-8").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration"))
        mockWebServer.start()
        val url = mockWebServer.url("/somehp5here").toString()


        presenter.handleUrlTextUpdated(url)

        verify(mockView, timeout(5000)).showUrlStatus(true, "")
        verify(mockView, timeout(5000)).displayUrl(url)


    }


    //20/Aug/2019: This test needs to be reworked after H5P and video import is live
    @Test
    fun givenUserClicksDone_whenH5PLinkIsValid_thenDownloadContent() {

        mockWebServer.enqueue(MockResponse().setHeader("content-type", "text/html; charset=utf-8").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.start()
        val url = mockWebServer.url("/somehp5here").toString()


        runBlocking {
            presenter.handleUrlTextUpdated(url).join()
            presenter.handleClickImport().join()
            Assert.assertTrue(defaultDb.contentEntryParentChildJoinDao.findListOfChildsByParentUuid(-101).isNotEmpty())
            Assert.assertTrue(serverdb.contentEntryParentChildJoinDao.findListOfChildsByParentUuid(-101).isNotEmpty())
            Assert.assertEquals("Func for h5p download called", 1, count)
        }

    }


    @Test
    fun givenUserClicksDone_whenVideoLinkValid_thenShowErrorIfUserDidntEnterTitle() {

        mockWebServer.enqueue(MockResponse().setHeader("content-length", 11).setHeader("content-type", "video/").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.start()
        val url = mockWebServer.url("/somehp5here").toString()

        runBlocking {
            presenter.handleUrlTextUpdated(url).join()
            presenter.handleClickImport().join()
            verify(mockView, timeout(5000)).showNoTitleEntered(UstadMobileSystemImpl.instance.getString(MessageID.import_title_not_entered, context))
        }
    }

    @Test
    fun givenUserClicksDone_whenVideoLinkValidAndTitleEntered_thenDownloadContent() {

        mockWebServer.enqueue(MockResponse().addHeader("content-length", 11).addHeader("content-type", "video/").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().addHeader("content-length", 11).addHeader("content-type", "video/").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("data"))
        mockWebServer.start()
        val url = mockWebServer.url("/somehp5here").toString()


        runBlocking {
            presenter.handleUrlTextUpdated(url).join()
            presenter.handleTitleChanged("Video Title")
            presenter.handleClickImport().join()
            Assert.assertTrue(defaultDb.contentEntryParentChildJoinDao.findListOfChildsByParentUuid(-101).isNotEmpty())
            Assert.assertTrue(serverdb.contentEntryParentChildJoinDao.findListOfChildsByParentUuid(-101).isNotEmpty())
            Assert.assertEquals("Func for h5p not called", 0, count)
        }


    }

    @Test
    fun givenUserClicksDone_whenVideoLinkValidAndTitleEntered_thenImportFail() {


        mockWebServer.enqueue(MockResponse().addHeader("Content-Length", 11).addHeader("Content-Type", "video/mp4").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("data").setResponseCode(404))
        mockWebServer.start()

        val args = Hashtable<String, String>()
        args[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = (-101).toString()
        presenter = ContentEntryImportLinkPresenter(context,
                args, mockView, mockWebServer.url("").toString())
        presenter.onCreate(args)

        val url = mockWebServer.url("/somehp5here").toString()

        runBlocking {
            presenter.handleUrlTextUpdated(url).join()
            presenter.handleTitleChanged("Video Title")
            presenter.handleClickImport().join()
            verify(mockView, timeout(5000)).showHideErrorMessage(false)
            verify(mockView, timeout(5000)).enableDisableEditText(true)
            verify(mockView, timeout(5000)).showHideErrorMessage(true)
        }


    }


}