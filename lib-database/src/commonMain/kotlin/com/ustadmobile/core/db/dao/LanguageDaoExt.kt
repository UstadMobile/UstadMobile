package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.Language

fun LanguageDao.initPreloadedLanguages() {
    val uidsInserted = findByUidList(Language.FIXED_LANGUAGES.map { it.langUid })
    val templateListToInsert = Language.FIXED_LANGUAGES.filter { it.langUid !in uidsInserted }
    replaceList(templateListToInsert)
}
