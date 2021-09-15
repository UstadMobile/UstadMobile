package com.ustadmobile.core.controller

import com.google.gson.Gson
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
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
                HttpClient(OkHttp) {
                    install(JsonFeature)
                    install(HttpTimeout)
                }
            }
        }


        presenter = ContentEntryImportLinkPresenter(context, mapOf(UstadView.ARG_RESULT_DEST_KEY to ""), mockView, di)



    }

    @Test
    fun givenPresenterCreated_whenUserEntersLinkAndIsValid_thenReturnToPreviousScreen() {

        val metadataResult = MetadataResult(ContentEntryWithLanguage(),EpubTypePluginCommonJvm.PLUGIN_ID)

        var response = MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
            .setBody(Buffer().write(
                safeStringify(di, MetadataResult.serializer(), metadataResult).toByteArray()))

        mockWebServer.enqueue(response)

        presenter.handleClickDone(mockWebServer.url("/").toString())

        verify(mockView, timeout(5000)).showHideProgress(false)
        verify(mockView, timeout(5000)).finishWithResult(metadataResult)
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