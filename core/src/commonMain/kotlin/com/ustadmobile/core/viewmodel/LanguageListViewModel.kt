package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.LanguageDaoCommon
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.lib.db.entities.Language

data class LanguageListUiState(
    val languageList: List<Language> = emptyList(),
    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MR.strings.name_key, LanguageDaoCommon.SORT_LANGNAME_ASC, true),
        SortOrderOption(MR.strings.name_key, LanguageDaoCommon.SORT_LANGNAME_DESC, false),
        SortOrderOption(MR.strings.two_letter_code, LanguageDaoCommon.SORT_TWO_LETTER_ASC, true),
        SortOrderOption(MR.strings.two_letter_code, LanguageDaoCommon.SORT_TWO_LETTER_DESC, false),
        SortOrderOption(MR.strings.three_letter_code, LanguageDaoCommon.SORT_THREE_LETTER_ASC, true),
        SortOrderOption(MR.strings.three_letter_code, LanguageDaoCommon.SORT_THREE_LETTER_DESC, false)
    ),
    val sortOrder: SortOrderOption = sortOptions.first()
)

class LanguageListViewModel {

    companion object {

        const val DEST_NAME = "LanguageList"
    }

}