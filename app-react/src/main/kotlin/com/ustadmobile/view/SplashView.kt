package com.ustadmobile.view

import com.ustadmobile.core.view.SplashScreenView

interface SplashView: SplashScreenView {

    var appName: String?

    fun showMainComponent()

    companion object {

        const val VIEW_NAME = "SplashView"

        const val TAG_LOADED = "app.loaded"
    }
}