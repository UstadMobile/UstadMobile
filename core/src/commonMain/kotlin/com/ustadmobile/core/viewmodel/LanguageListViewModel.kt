package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.LanguageDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.lib.db.entities.Language

data class LanguageListUiState(
    val languageList: List<Language> = emptyList(),
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