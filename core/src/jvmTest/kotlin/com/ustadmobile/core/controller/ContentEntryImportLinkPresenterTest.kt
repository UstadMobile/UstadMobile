package com.ustadmobile.core.controller

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify

class ContentEntryImportLinkPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ContentEntryImportLinkView

    private lateinit var context: Any

    private lateinit var di: DI

    private lateinit var presenter: ContentEntryImportLinkPresenter

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockView = mock { }
        context = Any()

        mockWebServer = MockWebServer()
        mockWebServer.start()

        di = DI {
            import(ustadTestRule.diModule)
        }
        val accountManager: UstadAccountManager by di.instance()
        accountManager.activeEndpoint = Endpoint(mockWebServer.url("/").toString())

        presenter = ContentEntryImportLinkPresenter(context, mapOf(UstadView.ARG_RESULT_DEST_KEY to ""), mockView, di)
    }

    @Test
    fun givenPresenterCreated_whenUserEntersLinkAndIsValid_thenReturnToPreviousScreen() {

        val metadataResult = MetadataResult(ContentEntryWithLanguage(),EpubTypePluginCommonJvm.PLUGIN_ID)

        val response = MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
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

    @After
    fun after(){
        mockWebServer.shutdown()
    }

}