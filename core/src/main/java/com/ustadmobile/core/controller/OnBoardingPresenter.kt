package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.DummyView
import com.ustadmobile.core.view.OnBoardingView

class OnBoardingPresenter(context: Any, arguments: Map<String, String>?, view: OnBoardingView) : UstadBaseController<OnBoardingView>(context, arguments!!, view) {

    override fun onCreate(savedState: Map<String, String> ?) {
        super.onCreate(savedState)
        view?.runOnUiThread(Runnable  { view?.setScreenList() })
    }


    fun handleGetStarted() {
        UstadMobileSystemImpl.instance.go(DummyView.VIEW_NAME, getContext())
    }
}
