package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.randomString


internal fun UmAppDatabase.insertDefaultSite() {
    siteDao().insert(Site().apply {
        siteUid = 1L
        siteName = "My Site"
        guestLogin = false
        registrationAllowed = false
        authSalt = randomString(20)
    })
}
