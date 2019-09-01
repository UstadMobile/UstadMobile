package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.SplashView
import kotlinx.coroutines.Runnable

class SplashPresenter(context: Any, arguments: Map<String, String?>, view: SplashView, val impl: UstadMobileSystemImpl)
    : UstadBaseController<SplashView>(context, arguments, view) {

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val launched = impl.getAppPref(OnBoardingView.PREF_TAG, "false",context).toBoolean()

        val showSplash = impl.getAppConfigString(AppConfig.KEY_SHOW_SPASH_SCREEN,
                "false", context)!!.toBoolean()

        val animateIcon = impl.getAppConfigString(AppConfig.KEY_ANIMATE_ORGANISATION_ICON,
                "false", context)!!.toBoolean()

        val delay = showSplash || !launched
        view.animateOrganisationIcon(animateIcon, delay)

        view.startUi(delay, animateIcon)
    }
}