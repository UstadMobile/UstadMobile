package com.ustadmobile.core.viewmodel.contententry.detailoverviewtab

import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ContentEntryDetailOverviewViewModelTest : AbstractMainDispatcherTest() {

    @Test
    fun givenContentEntryExists_whenLaunched_thenShouldShowContentEntry() {
        testViewModel<ContentEntryDetailOverviewViewModel> {
            val contentEntry = ContentEntry().apply {
                title = "Test entry"
                contentEntryUid = activeDb.contentEntryDao().insertAsync(this)
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = contentEntry.contentEntryUid.toString()
                ContentEntryDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived(timeout = 5.seconds) {
                it.contentEntry?.entry?.title == "Test entry"
            }
        }
    }

}