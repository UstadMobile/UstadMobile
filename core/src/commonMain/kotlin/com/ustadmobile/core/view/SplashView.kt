package com.ustadmobile.core.view

interface SplashView : UstadView {

    fun preloadData()

    fun startUi(delay: Boolean, animate: Boolean)

    fun animateOrganisationIcon(animate: Boolean, delay: Boolean)

    companion object {

        const val VIEW_NAME = "Splash"
    }
}
