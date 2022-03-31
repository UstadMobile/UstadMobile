package com.ustadmobile.core.controller

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.mockito.kotlin.*

class ContentEntryImportLinkPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ContentEntryImportLinkView

    private lateinit var context: Any

    private lateinit var di: DI

    private lateinit var presenter: ContentEntryImportLinkPresenter

    private lateinit var mockWebServer: MockWebServer

    private lateinit var savedStateHandle: UstadSavedStateHandle

    private lateinit var ustadBackStackEntry: UstadBackStackEntry

    private val resultKey = "MetaData"

    @Before
    fun setUp() {
        mockView = mock { }
        context = Any()

        savedStateHandle = mock{}

        ustadBackStackEntry = mock{
            on{savedStateHandle}.thenReturn(savedStateHandle)
        }

        mockWebServer = MockWebServer()
        mockWebServer.start()

        di = DI {
            import(ustadTestRule.diModule)
        }
        val accountManager: UstadAccountManager by di.instance()
        accountManager.activeEndpoint = Endpoint(mockWebServer.url("/").toString())

        presenter = ContentEntryImportLinkPresenter(context,
            mapOf(UstadView.ARG_RESULT_DEST_KEY to resultKey,
                UstadView.ARG_RESULT_DEST_VIEWNAME to ContentEntryImportLinkView.VIEW_NAME),
            mockView, di)
    }

    @Test
    fun givenPresenterCreated_whenUserEntersLinkAndIsValid_thenReturnToPreviousScreen() {
        val navController: UstadNavController = di.direct.instance()

        whenever(navController.getBackStackEntry(eq(ContentEntryImportLinkView.VIEW_NAME)))
            .thenReturn(ustadBackStackEntry)

        val metadataResult = MetadataResult(ContentEntryWithLanguage(),EpubTypePluginCommonJvm.PLUGIN_ID)

        val response = MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
            .setBody(Buffer().write(
                safeStringify(di, MetadataResult.serializer(), metadataResult).toByteArray()))

        mockWebServer.enqueue(response)

        presenter.handleClickDone(mockWebServer.url("/").toString())

        verify(mockView, timeout(5000)).inProgress = true

        verify(savedStateHandle, timeout(5000))[eq(resultKey)] = argWhere<String> {
            safeParseList(di, ListSerializer(MetadataResult.serializer()),
                MetadataResult::class, it).first() == metadataResult
        }
    }

    @Test
    fun givenPresenterCreated_whenUserEntersLinkAndIsInValid_thenShowError() {

        var response = MockResponse().setResponseCode(400)
        mockWebServer.enqueue(response)

        presenter.handleClickDone(mockWebServer.url("/").toString())

        verify(mockView, timeout(5000)).inProgress = true
        verify(mockView, timeout(5000)).validLink = false
        verify(mockView, timeout(5000)).inProgress = false
    }

    @After
    fun after(){
        mockWebServer.shutdown()
    }

}