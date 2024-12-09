package com.ustadmobile.core.viewmodel.contententry.getmetadata

import app.cash.turbine.test
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCase
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.kodein.di.bind
import org.kodein.di.provider
import org.kodein.di.scoped
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ContentEntryGetMetadataViewModelTest: AbstractMainDispatcherTest() {

    @Test
    fun givenMetadataExtractedSuccessfully_whenInitialized_thenShouldNavigateToContentEntryEdit() {
        val mockContentEntryGetMetaDataFromUriUseCase = mock<ContentEntryGetMetaDataFromUriUseCase> {
            onBlocking { invoke(any(), anyOrNull(), any(), any()) }
                .thenReturn(
                    MetadataResult(
                        entry = ContentEntryWithLanguage().apply {
                            title = "Test"
                        },
                        importerId = 42
                    )
                )
        }
        val argUri = "blob:file.epub"

        testViewModel<ContentEntryGetMetadataViewModel> {
            extendDi {
                bind<ContentEntryGetMetaDataFromUriUseCase>() with scoped(learningSpaceScope).provider {
                    mockContentEntryGetMetaDataFromUriUseCase
                }
            }

            viewModelFactory {
                savedStateHandle[ContentEntryGetMetadataViewModel.ARG_URI] = argUri
                ContentEntryGetMetadataViewModel(di, savedStateHandle)
            }

            val navigation = viewModel.navCommandFlow.first() as NavigateNavCommand
            assertEquals(ContentEntryEditViewModel.DEST_NAME, navigation.viewName)
            verifyBlocking(mockContentEntryGetMetaDataFromUriUseCase) {
                invoke(
                    contentUri = eq(DoorUri.parse(argUri)),
                    fileName = anyOrNull(),
                    learningSpace = any(),
                    onProgress = any(),
                )
            }
        }
    }

    @Test
    fun givenMetadataExtractionFails_whenInitialized_thenShouldShowErrorMessage() {
        val errorMessage = "snafu"
        val mockContentEntryGetMetaDataFromUriUseCase = mock<ContentEntryGetMetaDataFromUriUseCase> {
            onBlocking { invoke(any(), anyOrNull(), any(), any()) }.thenAnswer {
                throw Exception(errorMessage)
            }
        }

        val argUri = "blob:file.epub"

        testViewModel<ContentEntryGetMetadataViewModel> {
            extendDi {
                bind<ContentEntryGetMetaDataFromUriUseCase>() with scoped(learningSpaceScope).provider {
                    mockContentEntryGetMetaDataFromUriUseCase
                }
            }

            viewModelFactory {
                savedStateHandle[ContentEntryGetMetadataViewModel.ARG_URI] = argUri
                ContentEntryGetMetadataViewModel(di, savedStateHandle)
            }

            viewModel.uiState.filter { it.status.error != null }.test(
                timeout = 5.seconds, name = "error should show"
            ) {
                val state = awaitItem()
                assertTrue(state.status.error?.endsWith(errorMessage) == true,
                    "Error message is on UI state")
                cancelAndIgnoreRemainingEvents()
            }

            verifyBlocking(mockContentEntryGetMetaDataFromUriUseCase) {
                invoke(
                    contentUri = eq(DoorUri.parse(argUri)),
                    fileName = anyOrNull(),
                    learningSpace = any(),
                    onProgress = any(),
                )
            }
        }
    }

}