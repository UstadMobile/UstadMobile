package com.ustadmobile.core.viewmodel.contententry.importlink

import app.cash.turbine.test
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.contentformats.epub.EpubContentImporterCommonJvm
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel.Companion.ARG_NEXT
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds

class ContentEntryImportLinkViewModelTest: AbstractMainDispatcherTest() {

    private fun createMockMetadataResult(json: Json, mockContentTitle: String) =MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(
            json.encodeToString(
                serializer = MetadataResult.serializer(),
                value = MetadataResult(
                    entry = ContentEntryWithLanguage().apply {
                        title = mockContentTitle
                    },
                    importerId = EpubContentImporterCommonJvm.PLUGIN_ID
                )
            )
        )

    @Test
    fun givenNoExpectedResultArgs_whenUserEntersValidLinkAndClicksNext_thenWillGoToContentEntryEdit() {
        testViewModel<ContentEntryImportLinkViewModel> {
            viewModelFactory {
                savedStateHandle[ARG_NEXT] = ContentEntryEditViewModel.DEST_NAME
                ContentEntryImportLinkViewModel(di, savedStateHandle)
            }

            setActiveUser(
                learningSpace = LearningSpace(mockWebServer.url("/").toString()),
            )

            val mockContentTitle = "Ebook Title"
            val mockResponse = createMockMetadataResult(json, mockContentTitle)
            mockWebServer.enqueue(mockResponse)

            val linkToImport = "https://server.com/file.epub"

            viewModel.onChangeLink(linkToImport)
            viewModel.onClickNext()
            viewModel.navCommandFlow.test(name = "receive navigation command", timeout = 5.seconds) {
                val item = awaitItem()
                val command = item as NavigateNavCommand
                assertEquals(ContentEntryEditViewModel.DEST_NAME, command.viewName)
                val metaDataArg = command.args[ContentEntryEditViewModel.ARG_IMPORTED_METADATA]?.let {
                    json.decodeFromString(MetadataResult.serializer(), it)
                }
                assertEquals(mockContentTitle, metaDataArg?.entry?.title)

                cancelAndIgnoreRemainingEvents()
            }

            val requestReceived = mockWebServer.takeRequest()
            assertEquals(linkToImport, requestReceived.requestUrl?.queryParameter("url"))

        }
    }

    @Test
    fun givenNoExpectedResultArg_whenUserEntersInvalidLink_thenWillShowError() {
        testViewModel<ContentEntryImportLinkViewModel> {
            viewModelFactory {
                ContentEntryImportLinkViewModel(di, savedStateHandle)
            }

            setActiveUser(
                learningSpace = LearningSpace(mockWebServer.url("/").toString()),
            )

            val mockResponse = MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "text/plain")
                .setBody("")
            mockWebServer.enqueue(mockResponse)
            val linkToImport = "https://server.com/file.epub"

            viewModel.onChangeLink(linkToImport)
            viewModel.onClickNext()

            viewModel.uiState.assertItemReceived(
                timeout = 50.seconds,
                name = "When server responds to indicate the link is invalid, the UI state is updated accordingly"
            ) {
                it.linkError != null &&
                it.url == linkToImport
            }

            val requestReceived = mockWebServer.takeRequest()
            assertEquals(linkToImport, requestReceived.requestUrl?.queryParameter("url"))
        }
    }

    @Test
    fun givenExcpectedResultArg_whenUserEntersValidLink_thenWillReturnResult() {
        testViewModel<ContentEntryImportLinkViewModel> {
            val navResultKey = "metadata"
            viewModelFactory {
                savedStateHandle[UstadView.ARG_RESULT_DEST_VIEWNAME] = ContentEntryEditViewModel.DEST_NAME
                savedStateHandle[UstadView.ARG_RESULT_DEST_KEY] = navResultKey
                ContentEntryImportLinkViewModel(di, savedStateHandle)
            }

            setActiveUser(
                learningSpace = LearningSpace(mockWebServer.url("/").toString()),
            )


            val mockContentTitle = "Updated content title"
            val mockResponse = createMockMetadataResult(json, mockContentTitle)
            mockWebServer.enqueue(mockResponse)


            val linkToImport = "https://server.com/file.epub"

            viewModel.onChangeLink(linkToImport)
            viewModel.onClickNext()

            viewModel.navCommandFlow.test(
                timeout = 50.seconds,
                name = "When user enters valid link and navigation result is expected, then finishWithResult is called"
            ) {
                val navCommand = awaitItem() as PopNavCommand
                assertEquals(ContentEntryEditViewModel.DEST_NAME, navCommand.viewName)
                assertFalse(navCommand.inclusive)
                cancelAndIgnoreRemainingEvents()
            }

            verify(navResultReturner).sendResult(argWhere {
                val result = it.result as MetadataResult
                it.key == navResultKey && result.entry.title == mockContentTitle
            })

            val requestReceived = mockWebServer.takeRequest()
            assertEquals(linkToImport, requestReceived.requestUrl?.queryParameter("url"))
        }
    }
}