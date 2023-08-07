package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.LanguageDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.lib.db.entities.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class LanguageListUiState(
    val languageList: List<Language> = listOf(
        Language().apply {
            name = "فارسی"
            langUid = Language.PERSIAN_LANG_UID
            iso_639_1_standard = "fa"
            iso_639_2_standard = "per"
            iso_639_3_standard = "fas"
        },
        Language().apply{
            name = "English"
            langUid = Language.ENGLISH_LANG_UID
            iso_639_1_standard = "en"
            iso_639_2_standard = "eng"
            iso_639_3_standard = "eng"
        },
        Language().apply{
            name = "العربية"
            langUid = Language.ARABIC_LANG_UID
            iso_639_1_standard = "ar"
            iso_639_2_standard = "ara"
            iso_639_3_standard = "ara"
        }
    ),
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
) : UstadViewModel(di, savedStateHandle, LanguageListView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(LanguageListUiState())

    val uiState: Flow<LanguageListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                val languages = activeRepo.languageDao.findLanguagesList()

                _uiState.update {prev ->
                    prev.copy(
                        languageList = languages
                    )
                }
            }
        }
    }

    fun onListItemClick(language: Language) {

    }

    fun onClickSort() {

    }

}