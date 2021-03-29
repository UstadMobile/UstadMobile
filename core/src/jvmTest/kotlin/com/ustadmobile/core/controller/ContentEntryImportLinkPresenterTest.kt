package com.ustadmobile.core.controller

import com.google.gson.Gson
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.*
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class ContentEntryImportLinkPresenterTest {


    private lateinit var mockView: ContentEntryImportLinkView

    private lateinit var accountManager: UstadAccountManager

    private lateinit var context: Any

    private lateinit var di: DI

    private lateinit var presenter: ContentEntryImportLinkPresenter

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        context = Any()

        mockView = mock { }

        mockWebServer = MockWebServer()
        mockWebServer.start()

        accountManager = mock{
            on{activeAccount}.thenReturn(UmAccount(0,"","",mockWebServer.url("/").toString()))
        }

        di = DI {
            bind<UstadAccountManager>() with singleton { accountManager }
            bind<Gson>() with singleton { Gson() }
            bind<HttpClient>() with singleton {
                defaultHttpClient()
            }
        }


        presenter = ContentEntryImportLinkPresenter(context, mapOf(), mockView, di)



    }

    @Test
    fun givenPresenterCreated_whenUserEntersLinkAndIsValid_thenReturnToPreviousScreen() {

        var importedContentEntryMetaData = ImportedContentEntryMetaData(
                ContentEntryWithLanguage(), "application/epub+zip",
                "file://abc.zip", "googleDriveScraper")

        var response = MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
            .setBody(Buffer().write(
                safeStringify(di, ImportedContentEntryMetaData.serializer(), importedContentEntryMetaData).toByteArray()))

        mockWebServer.enqueue(response)

        presenter.handleClickDone(mockWebServer.url("/").toString())

        verify(mockView, timeout(5000)).showHideProgress(false)
        verify(mockView, timeout(5000)).finishWithResult(importedContentEntryMetaData)
    }

    @Test
    fun givenPresenterCreated_whenUserEntersLinkAndIsInValid_thenShowError() {

        var response = MockResponse().setResponseCode(400)
        mockWebServer.enqueue(response)

        presenter.handleClickDone(mockWebServer.url("/").toString())

        verify(mockView, timeout(5000)).showHideProgress(false)
        verify(mockView, timeout(5000)).validLink = false
        verify(mockView, timeout(5000)).showHideProgress(true)
    }

}