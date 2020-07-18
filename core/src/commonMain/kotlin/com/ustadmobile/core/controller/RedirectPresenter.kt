package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_FROM
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import org.kodein.di.DI
import org.kodein.di.instance

class RedirectPresenter(context: Any, arguments: Map<String, String>, view: RedirectView,
                        di: DI) :
        UstadBaseController<RedirectView>(context, arguments, view, di) {

    val systemImpl: UstadMobileSystemImpl by di.instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val cameFromGetStarted = arguments[ARG_FROM] == OnBoardingView.VIEW_NAME
        val nextViewArg = arguments[ARG_NEXT]

        val canSelectServer = systemImpl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION,
                context)

        val args = mutableMapOf<String, String>()
        val destination = if(nextViewArg != null) {
            args.putAll(UMFileUtil.parseURLQueryString(nextViewArg))
            nextViewArg.substringBefore('?')
        }else if(cameFromGetStarted){
            if(canSelectServer) GetStartedView.VIEW_NAME else Login2View.VIEW_NAME
        }else {
            ContentEntryListTabsView.VIEW_NAME
        }

        view.showNextScreen(destination, args)
    }
}
