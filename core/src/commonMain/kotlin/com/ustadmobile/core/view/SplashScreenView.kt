package com.ustadmobile.core.view

interface SplashScreenView : UstadView {

    fun startUi(delay: Boolean, animate: Boolean)

    fun animateOrganisationIcon(animate: Boolean, delay: Boolean)

    companion object {

        const val VIEW_NAME = "Splash"
    }
}
