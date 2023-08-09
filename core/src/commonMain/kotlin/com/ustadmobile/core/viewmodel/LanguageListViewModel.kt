package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.LanguageDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class LanguageListUiState(
    val languageList: () -> PagingSource<Int, Language> = { EmptyPagingSource() },
    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MessageID.name, LanguageDaoCommon.SORT_LANGNAME_ASC, true),
        SortOrderOption(MessageID.name, LanguageDaoCommon.SORT_LANGNAME_DESC, false),
        SortOrderOption(MessageID.two_letter_code, LanguageDaoCommon.SORT_TWO_LETTER_ASC, true),
        SortOrderOption(MessageID.two_letter_code, LanguageDaoCommon.SORT_TWO_LETTER_DESC, false),
        SortOrderOption(MessageID.three_letter_code, LanguageDaoCommon.SORT_THREE_LETTER_ASC, true),
        SortOrderOption(MessageID.three_letter_code, LanguageDaoCommon.SORT_THREE_LETTER_DESC, false)
    ),
    val sortOrder: SortOrderOption = sortOptions.first()
)

class LanguageListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadListViewModel<LanguageListUiState>(di, savedStateHandle, LanguageListUiState(), "LanguageList") {

    private val pagingSourceFactory: () -> PagingSource<Int, Language> = {
        activeRepo.languageDao.findLanguageListAsPagingSource().also {
            lastPagingSource?.invalidate()
            lastPagingSource = it
        }
    }

    private var lastPagingSource: PagingSource<Int, Language>? = null

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    _uiState.update { prev ->
                        prev.copy(
                            languageList = pagingSourceFactory
                        )
                    }
                }
            }
        }
    }

    fun onListItemClick(language: Language) {

    }

    fun onClickSort(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(
                sortOrder = sortOption
            )
        }
        lastPagingSource?.invalidate()
    }

    override fun onUpdateSearchResult(searchText: String) {
        lastPagingSource?.invalidate()
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }

}