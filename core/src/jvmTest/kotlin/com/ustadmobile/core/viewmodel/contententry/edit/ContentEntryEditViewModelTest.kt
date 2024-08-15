package com.ustadmobile.core.viewmodel.contententry.edit

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.save.SaveContentEntryUseCase
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.SystemPermission
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.kodein.di.bind
import org.kodein.di.scoped
import org.kodein.di.singleton
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals

class ContentEntryEditViewModelTest : AbstractMainDispatcherTest(){

    /**
     * Where ARG_IMPORTED_METADATA is provided, then content should be saved and import use case
     * should be invoked
     */
    @Test
    fun givenImportedMetadataArgProvided_whenSaved_thenShouldSaveToDatabaseAndCallImportContent() {
        val metadata = MetadataResult(
            entry = ContentEntryWithLanguage().apply {
                title = "File Title"
                sourceUrl = "https://server.com/file.epub"
            },
            importerId = 42
        )
        testViewModel<ContentEntryEditViewModel> {
            val mockSaveContentEntryUseCase = mock<SaveContentEntryUseCase>()
            val mockImportContentUseCase = mock<EnqueueContentEntryImportUseCase>()
            val user = setActiveUser(activeLearningSpace)
            activeDb.systemPermissionDao().upsertAsync(
                SystemPermission(
                    spToPersonUid = user.personUid,
                    spPermissionsFlag = PermissionFlags.EDIT_LIBRARY_CONTENT,
                )
            )

            extendDi {
                bind<SaveContentEntryUseCase>() with scoped(learningSpaceScope).singleton {
                    mockSaveContentEntryUseCase
                }

                bind<EnqueueContentEntryImportUseCase>() with scoped(learningSpaceScope).singleton {
                    mockImportContentUseCase
                }
            }

            viewModelFactory {
                savedStateHandle[ContentEntryEditViewModel.ARG_IMPORTED_METADATA] = json.encodeToString(
                    serializer = MetadataResult.serializer(),
                    value = metadata
                )
                ContentEntryEditViewModel(di, savedStateHandle)
            }

            val enabledUiState = withTimeout(5000) {
                viewModel.uiState.filter { it.fieldsEnabled }.first()
            }

            viewModel.onContentEntryChanged(
                enabledUiState.entity?.entry?.shallowCopy {
                    description = "new description"
                }
            )

            viewModel.onClickSave()

            var contentEntryUid: Long = 0
            argumentCaptor<ContentEntry> {
                verifyBlocking(mockSaveContentEntryUseCase, timeout(5000)) {
                    invoke(capture(), anyOrNull(), anyOrNull(), anyOrNull())
                    contentEntryUid = firstValue.contentEntryUid
                    assertEquals("new description", firstValue.description)
                    assertEquals(metadata.entry.sourceUrl, firstValue.sourceUrl)
                }
            }

            verifyBlocking(mockImportContentUseCase, timeout(5000)) {
                invoke(
                    contentJobItem = argWhere {
                        it.sourceUri == metadata.entry.sourceUrl &&
                                it.cjiContentEntryUid == contentEntryUid
                    }
                )
            }
        }
    }

}