package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*


class ContentEntryImportLinkPresenterTest {

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private var mockView: ContentEntryImportLinkView = mock {}

    private lateinit var presenter: ContentEntryImportLinkPresenter

    var server = MockWebServer()

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

        val args = Hashtable<String, String>()
        presenter = ContentEntryImportLinkPresenter(context,
                args, mockView)
        presenter.onCreate(args)
    }

    @After
    fun after() {
        server.shutdown()
    }


    @Test
    fun givenWhenHandleUrlTextUpdated_whenEmptyScreen_showInvalidUrlMessage() {
        runBlocking {
            presenter.handleUrlTextUpdated("")
            verify(mockView).showUrlStatus(false, "Invalid Url")
        }


    }

    @Test
    fun givenWhenHandleUrlTextUpdated_whenNotHp5_showUnSupportedContent() {

        server.enqueue(MockResponse().setBody("no h5p here").setResponseCode(200))
        server.start()

        runBlocking {
            presenter.handleUrlTextUpdated(server.url("/nohp5here").toString())

            verify(mockView, timeout(15000)).showUrlStatus(false, "Content not supported")
        }


    }

    @Test
    fun givenWhenHandleUrlTextUpdated_whenHp5_showValidAndNoMessage() {

        server.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        server.start()
        val url = server.url("/somehp5here").toString()


        runBlocking {
            presenter.handleUrlTextUpdated(url)

            verify(mockView, timeout(15000)).showUrlStatus(true, "")
            verify(mockView, timeout(15000)).displayUrl(url)

        }

    }


}