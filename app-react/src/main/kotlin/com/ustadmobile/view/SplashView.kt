package com.ustadmobile.view

import com.ustadmobile.core.view.UstadView

interface SplashView: UstadView {
    var appName: String?
    fun showMainComponent()
}