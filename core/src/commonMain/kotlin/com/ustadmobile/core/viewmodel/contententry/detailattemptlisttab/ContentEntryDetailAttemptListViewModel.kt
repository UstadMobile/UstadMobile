package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewUiState
import com.ustadmobile.lib.db.composites.ContentEntryAndDetail
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class ContentEntryDetailAttemptListUiState(
    val statementEntity: StatementEntity? = null
)


class ContentEntryDetailAttemptListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) :
    DetailViewModel<ContentEntry>(di, savedStateHandle, DEST_NAME) {
    private val _uiState = MutableStateFlow(
        ContentEntryDetailAttemptListUiState()
    )
    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.statementDao().getOneStatement().collect {
                        _uiState.update { prev ->
                            prev.copy(
                                statementEntity = it
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {

        const val DEST_NAME = "ContentEntryDetailAttemptList"

    }
}