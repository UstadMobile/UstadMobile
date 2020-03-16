package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.SplashScreenView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenPresenter(context: Any, arguments: Map<String, String?>, view: SplashScreenView,
                            val impl: UstadMobileSystemImpl, val delay: Long = DEFAULT_DELAY)
    : UstadBaseController<SplashScreenView>(context, arguments, view) {

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val onboardingShown = impl.getAppPref(OnBoardingView.PREF_TAG, "false",context).toBoolean()

        GlobalScope.launch {
            delay(delay)

            if(!onboardingShown) {
                impl.go(OnBoardingView.VIEW_NAME, mapOf(), context)
            }else {
                impl.startUI(context)
            }
        }
    }

    companion object {

        const val DEFAULT_DELAY = 2000L

    }
}