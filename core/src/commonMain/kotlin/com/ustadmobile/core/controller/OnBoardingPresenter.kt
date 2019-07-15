package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.OnBoardingView.Companion.PREF_TAG
import kotlinx.coroutines.Runnable

class OnBoardingPresenter(context: Any, arguments: Map<String, String>?, view: OnBoardingView)
    : UstadBaseController<OnBoardingView>(context, arguments!!, view) {

    val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance
    override fun onCreate(savedState: Map<String, String?> ?) {
        super.onCreate(savedState)
        view.runOnUiThread(Runnable  { view.setScreenList() })

        val wasShown = impl.getAppPref(PREF_TAG, view.viewContext)
        if (wasShown!= null && wasShown.toBoolean()) {
            handleGetStarted()
        }
    }


    fun handleGetStarted() {
        val args: Map<String,String?> = arguments;
        impl.setAppPref(PREF_TAG, true.toString(), view.viewContext)
        impl.go(HomeView.VIEW_NAME, args, context)
    }
}
