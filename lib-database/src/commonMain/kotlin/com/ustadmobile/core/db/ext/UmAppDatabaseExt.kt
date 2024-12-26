package com.ustadmobile.core.db.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.initPreloadedLanguages
import com.ustadmobile.core.db.dao.initPreloadedLeavingReasons

suspend fun UmAppDatabase.preload() {
//    reportDao().initPreloadedTemplates()
    leavingReasonDao().initPreloadedLeavingReasons()
    languageDao().initPreloadedLanguages()

}