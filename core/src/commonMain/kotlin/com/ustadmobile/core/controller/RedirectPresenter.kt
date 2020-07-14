package com.ustadmobile.core.controller

import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView.Companion.ARG_FROM
import org.kodein.di.DI

class RedirectPresenter(context: Any, arguments: Map<String, String>, view: RedirectView,
                        di: DI) :
        UstadBaseController<RedirectView>(context, arguments, view, di) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val fromView = arguments[ARG_FROM]
        view.showGetStarted = fromView != null && fromView == OnBoardingView.VIEW_NAME
    }
}
