package com.ustadmobile.jsExt

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.view.SplashView
import kotlinx.browser.localStorage

suspend fun UmAppDatabase.initJsRepo(impl: UstadMobileSystemImpl){
    if(!impl.getAppPref(SplashView.TAG_LOADED, this).toBoolean()){
        localStorage.clear()
        roleDao.insertDefaultRolesIfRequired()
        impl.setAppPref(SplashView.TAG_LOADED,"true", this)
        preload()

        siteDao.insertAsync(Site().apply {
            siteUid = 1L
            siteName = "My Site"
            guestLogin = false
            registrationAllowed = false
            authSalt = randomString(20)
        })
    }
}