package com.ustadmobile.core.viewmodel.contententry.importlink

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import okhttp3.mockwebserver.MockResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ContentEntryImportLinkViewModelTest: AbstractMainDispatcherTest() {

    @Test
    fun givenNoExpectedResultArgs_whenUserEntersValidLinkAndClicksNext_thenWillGoToContentEntryEdit() {
        testViewModel<ContentEntryImportLinkViewModel> {
            viewModelFactory {
                ContentEntryImportLinkViewModel(di, savedStateHandle)
            }

            setActiveUser(
                endpoint = Endpoint(mockWebServer.url("/").toString()),
            )

            val mockResponse = MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    json.encodeToString(
                        serializer = MetadataResult.serializer(),
                        value = MetadataResult(
                            ContentEntryWithLanguage(), EpubTypePluginCommonJvm.PLUGIN_ID
                        )
                    )
                )
            mockWebServer.enqueue(mockResponse)

            viewModel.onChangeLink("https://server.com/file.epub")
            viewModel.onClickNext()
            viewModel.navCommandFlow.test(name = "receive navigation command", timeout = 5.seconds) {
                val item = awaitItem()
                val command = item as NavigateNavCommand
                assertEquals(ContentEntryEditViewModel.DEST_NAME, command.viewName)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

}