package com.ustadmobile.core.viewmodel.xapicontent

import app.cash.turbine.test
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.flow.filter
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.bind
import org.kodein.di.singleton
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class XapiContentViewModelTest : AbstractMainDispatcherTest() {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @Test
    fun givenValidXapiUrl_whenStarted_thenShouldSetContentUrl() {
        val xapiTmpFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contentformats/xapi/tincan.xml")
        val xapiXmlStr = xapiTmpFile.readText()

        testViewModel<XapiContentViewModel> {
            extendDi {
                bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
                    XmlPullParserFactory.newInstance().also {
                        it.isNamespaceAware = true
                    }
                }
            }

            mockWebServer.dispatcher = object: Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    return MockResponse()
                        .addHeader("content-type", "application/xml")
                        .setBody(xapiXmlStr)
                }
            }

            val cevUid = activeDb.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
            val contentEntryVersion = ContentEntryVersion(
                cevUid = cevUid,
                cevUrl = mockWebServer.url("/$cevUid/tincan.xml").toString(),
            )
            activeDb.contentEntryVersionDao.insertAsync(contentEntryVersion)

            viewModelFactory {
                savedStateHandle[UstadViewModel.ARG_ENTITY_UID] = cevUid.toString()
                XapiContentViewModel(di, savedStateHandle)
            }

            viewModel.uiState.filter {
                it.url != null
            }.test(timeout = 5.seconds, name = "url set should match that specified in Xml") {
                val state = awaitItem()
                assertEquals(
                    mockWebServer.url("/$cevUid/tetris.html").toString(),
                    state.url
                )
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

}