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
            val canSelectServer = systemImpl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION,
                    context)

            GlobalScope.launch(doorMainDispatcher()) {
                val numActiveAccounts = accountManager.activeSessionCount()
                val destination = nextViewArg ?: if (numActiveAccounts < 1) {
                    if (canSelectServer)
                        SiteEnterLinkView.VIEW_NAME
                    else
                        Login2View.VIEW_NAME
                } else {
                    ContentEntryListTabsView.VIEW_NAME
                }

                systemImpl.goToViewLink(destination, context)
            }



//            val userHasLoggedInOrSelectedGuest = systemImpl.getAppPref(
//                    Login2Presenter.PREFKEY_USER_LOGGED_IN, "false", context).toBoolean()
//
//            val destination = nextViewArg ?: if (!userHasLoggedInOrSelectedGuest) {
//                if (canSelectServer)
//                    SiteEnterLinkView.VIEW_NAME
//                else
//                    Login2View.VIEW_NAME
//            } else {
//                ContentEntryListTabsView.VIEW_NAME
//            }


        }
    }

}
