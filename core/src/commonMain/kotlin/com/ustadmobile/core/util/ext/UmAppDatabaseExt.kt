package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.UmAppDatabase

fun UmAppDatabase.runPreload() {
    preload()
    timeZoneEntityDao.insertSystemTimezones()
}
