package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_DEEPLINK
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.door.doorMainDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class RedirectPresenter(context: Any, arguments: Map<String, String>, view: RedirectView,
                        di: DI) :
        UstadBaseController<RedirectView>(context, arguments, view, di) {

    val systemImpl: UstadMobileSystemImpl by instance()

    val accountManager: UstadAccountManager by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val nextViewArg = arguments[ARG_NEXT]
        val deepLink = arguments[ARG_DEEPLINK]

        if(deepLink?.isNotEmpty() == true){
            systemImpl.goToDeepLink(deepLink, accountManager, context)
        }else {
            if(nextViewArg != null) {
                systemImpl.goToViewLink(nextViewArg, context)
            }else if(accountManager.activeSession != null) {
                systemImpl.goToViewLink(ContentEntryListTabsView.VIEW_NAME, context)
            }else {
                presenterScope.launch {
                    navigateToStartNewUserSession()
                }
            }
        }
    }

}
