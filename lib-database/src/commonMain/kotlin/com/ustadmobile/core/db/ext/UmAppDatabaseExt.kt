package com.ustadmobile.core.db.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.initPreloadedLanguages
import com.ustadmobile.core.db.dao.initPreloadedLeavingReasons
import com.ustadmobile.core.db.dao.initPreloadedTemplates
import com.ustadmobile.core.db.dao.initPreloadedVerbs

suspend fun UmAppDatabase.preload() {
    verbDao.initPreloadedVerbs()
    reportDao.initPreloadedTemplates()
    leavingReasonDao.initPreloadedLeavingReasons()
    languageDao.initPreloadedLanguages()

}