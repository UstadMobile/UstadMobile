package com.ustadmobile.core.viewmodel.xapicontent

import app.cash.turbine.test
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.ContentManifestEntry
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.ResolveXapiLaunchHrefUseCase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.xapi.session.ResumeOrStartXapiSessionUseCase
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.stringvalues.emptyStringValues
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.flow.filter
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.test.Test
import kotlin.test.assertTrue
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

                bind<ResolveXapiLaunchHrefUseCase>() with scoped(learningSpaceScope).singleton {
                    ResolveXapiLaunchHrefUseCase(
                        activeRepoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
                        httpClient = instance(),
                        json = instance(),
                        xppFactory = instance(tag = DiTag.XPP_FACTORY_NSAWARE),
                        getApiUrlUseCase = instance(),
                        learningSpace = context,
                        accountManager = instance(),
                        resumeOrStartXapiSessionUseCase = instance(),
                    )
                }

                bind<GetApiUrlUseCase>() with scoped(learningSpaceScope).singleton {
                    mock {
                        onBlocking { invoke(any()) }.thenReturn(
                            "http://localhost/e/endpoint/xapi"
                        )
                    }
                }

                bind<ResumeOrStartXapiSessionUseCase>() with scoped(learningSpaceScope).singleton {
                    mock {
                        onBlocking { invoke(any(), any(), any(), any(), any(), any()) }.thenReturn(
                            XapiSessionEntity(
                                xseAccountUsername = "admin",

                            )
                        )
                    }
                }
            }

            val cevUid = activeDb.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
            val contentEntryVersion = ContentEntryVersion(
                cevUid = cevUid,
                cevManifestUrl = mockWebServer.url("/$cevUid/${ContentConstants.MANIFEST_NAME}").toString(),
                cevOpenUri = "tincan.xml",
            )
            activeDb.contentEntryVersionDao().insertAsync(contentEntryVersion)
            val contentManifest = ContentManifest(
                version = 1,
                metadata = emptyMap(),
                entries = listOf(
                    ContentManifestEntry(
                        uri = "tincan.xml",
                        integrity = "foo",
                        responseHeaders = emptyStringValues(),
                        bodyDataUrl = mockWebServer.url("/tincan.xml").toString(),
                        storageSize = 1_000,
                    )
                )
            )


            mockWebServer.dispatcher = object: Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val requestUrl = request.requestUrl?.toString()
                    return when {
                        requestUrl?.endsWith("tincan.xml") == true -> {
                            MockResponse()
                                .addHeader("content-type", "application/xml")
                                .setBody(xapiXmlStr)
                        }
                        requestUrl?.endsWith(ContentConstants.MANIFEST_NAME) == true -> {
                            MockResponse()
                                .addHeader("content-type", "application/json")
                                .setBody(
                                    json.encodeToString(ContentManifest.serializer(), contentManifest)
                                )
                        }
                        else -> MockResponse().setResponseCode(404).setBody("")
                    }
                }
            }



            viewModelFactory {
                savedStateHandle[UstadViewModel.ARG_ENTITY_UID] = cevUid.toString()
                XapiContentViewModel(di, savedStateHandle)
            }

            viewModel.uiState.filter {
                it.url != null
            }.test(timeout = 5.seconds, name = "url set should match that specified in Xml") {
                val state = awaitItem()
                assertTrue(state.url!!.startsWith(mockWebServer.url("/$cevUid/tetris.html").toString()))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

}