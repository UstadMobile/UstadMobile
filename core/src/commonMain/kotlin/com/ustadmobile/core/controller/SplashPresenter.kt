package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.SplashView
import kotlinx.coroutines.Runnable

class SplashPresenter(context: Any, arguments: Map<String, String?>, view: SplashView)
    : UstadBaseController<SplashView>(context, arguments, view) {

    val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val launched = impl.getAppPref(OnBoardingView.PREF_TAG, "false",context).toBoolean()

        val showSplash = impl.getAppConfigString(AppConfig.KEY_SHOW_SPASH_SCREEN,
                null, context)!!.toBoolean()

        val animateIcon = impl.getAppConfigString(AppConfig.KEY_ANIMATE_ORGANISATION_ICON,
                null, context)!!.toBoolean()

        view.runOnUiThread(Runnable {
            view.animateOrganisationIcon(animateIcon)
        })

        if(!launched){
            view.preloadData()
        }

        view.startUi(showSplash || !launched, animateIcon)
    }
}