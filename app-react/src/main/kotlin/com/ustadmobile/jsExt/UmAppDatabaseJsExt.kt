package com.ustadmobile.jsExt

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.randomString
import kotlinx.browser.localStorage

suspend fun UmAppDatabase.initJsRepo(preloaded: Boolean){
    if(!preloaded){
        localStorage.clear()
        roleDao.insertDefaultRolesIfRequired()
        preload()
    }

    val site = siteDao.getSiteAsync()
    if(site == null){
        siteDao.insertAsync(Site().apply {
            siteUid = 1L
            siteName = "My Site"
            guestLogin = false
            registrationAllowed = false
            authSalt = randomString(20)
        })
    }
}