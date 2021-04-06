package com.ustadmobile.view

import com.ustadmobile.core.view.UstadView

interface SplashView: UstadView {
    var appName: String?
    fun showMainComponent()

    companion object {
        const val LOADED_TAG = "app.loaded"
    }
}