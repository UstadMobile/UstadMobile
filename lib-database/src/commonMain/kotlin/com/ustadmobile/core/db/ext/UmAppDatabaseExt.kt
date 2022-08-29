package com.ustadmobile.core.db.ext

import com.ustadmobile.core.db.UmAppDatabase

suspend fun UmAppDatabase.preload() {
    verbDao.initPreloadedVerbs()
    reportDao.initPreloadedTemplates()
    leavingReasonDao.initPreloadedLeavingReasons()
    languageDao.initPreloadedLanguages()

}