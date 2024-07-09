package com.ustadmobile.core.viewmodel.videocontent

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.ContentManifestEntry
import com.ustadmobile.core.contentformats.media.MediaContentInfo
import com.ustadmobile.core.contentformats.media.MediaSource
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.ext.resultDurationMillis
import com.ustadmobile.core.domain.xapi.ext.resultProgressExtension
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.noninteractivecontentusagestatementrecorder.NonInteractiveContentXapiStatementRecorderFactory
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCase
import com.ustadmobile.core.test.isWithinThreshold
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.stringvalues.emptyStringValues
import com.ustadmobile.core.viewmodel.UstadViewModel.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import kotlin.test.Test

class VideoContentViewModelTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    inner class VideoTestMockWebServerDispatcher(
        private val manifest: ContentManifest
    ): Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val requestUrlVal = request.requestUrl ?: return MockResponse().setResponseCode(500)

            return when {
                requestUrlVal.toString().endsWith(ContentConstants.MANIFEST_NAME) -> {
                    MockResponse()
                        .addHeader("content-type", "application/json")
                        .setBody(json.encodeToString(manifest))
                }

                requestUrlVal.toString().endsWith("mediainfo.json") -> {
                    val mediaInfo = MediaContentInfo(
                        listOf(MediaSource(uri = "video.mp4", mimeType = "video/mp4"))
                    )

                    MockResponse()
                        .addHeader("content-type", "application/json")
                        .setBody(json.encodeToString(mediaInfo))
                }
                else -> MockResponse().setResponseCode(404)
            }
        }
    }

    @Test
    fun givenVideoPlayStarted_whenCleared_thenShouldRecordStatement() {
        testViewModel<VideoContentViewModel> {
            val mockSaveOnClearUseCase = mock<SaveStatementOnClearUseCase>()
            val mockStatementResource = mock<XapiStatementResource>()

            extendDi {
                bind<SaveStatementOnClearUseCase>() with scoped(endpointScope).singleton {
                    mockSaveOnClearUseCase
                }

                bind<XapiStatementResource>() with scoped(endpointScope).singleton {
                    mockStatementResource
                }

                bind<NonInteractiveContentXapiStatementRecorderFactory>() with scoped(endpointScope).singleton {
                    NonInteractiveContentXapiStatementRecorderFactory(
                        saveStatementOnClearUseCase = instance(),
                        saveStatementOnUnloadUseCase = null,
                        xapiStatementResource = instance()
                    )
                }
            }

            val cevUid = activeDb.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
            val contentEntryVersion = ContentEntryVersion(
                cevUid = cevUid,
                cevManifestUrl = mockWebServer.url("/$cevUid/${ContentConstants.MANIFEST_NAME}").toString(),
                cevOpenUri = "mediainfo.json",
            )
            activeDb.contentEntryVersionDao().insertAsync(contentEntryVersion)

            val contentManifest = ContentManifest(
                version = 1,
                metadata = emptyMap(),
                entries = listOf(
                    ContentManifestEntry(
                        uri = "mediainfo.json",
                        integrity = "foo",
                        responseHeaders = emptyStringValues(),
                        bodyDataUrl = mockWebServer.url("/mediainfo.json").toString(),
                        storageSize = 1_000,
                    ),
                    ContentManifestEntry(
                        uri = "video.mp4",
                        integrity = "foo2",
                        responseHeaders = emptyStringValues(),
                        bodyDataUrl = mockWebServer.url("/video.mp4").toString(),
                        storageSize = 1_000,
                    )
                )
            )

            mockWebServer.dispatcher = VideoTestMockWebServerDispatcher(contentManifest)

            viewModelFactory {
                savedStateHandle[ARG_ENTITY_UID] = cevUid.toString()
                VideoContentViewModel(di, savedStateHandle)
            }

            //Wait for viewmodel to setup
            viewModel.uiState.assertItemReceived { it.mediaContentInfo != null }

            viewModel.onPlayStateChanged(
                VideoContentViewModel.MediaPlayState(totalDuration = 5_000, resumed = true)
            )
            val delay = 1_000L
            delay(delay)
            viewModel.onPlayStateChanged(
                VideoContentViewModel.MediaPlayState(
                    totalDuration = 5_000, resumed = true, timeInMillis = 1_000,
                )
            )
            viewModel.onClear()
            verify(mockSaveOnClearUseCase, timeout(5_000)).invoke(
                statements = argWhere { stmts ->
                    isWithinThreshold(delay, stmts.first().resultDurationMillis!!, 100)
                },
                xapiSession = any(),
            )
        }
    }

}