package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_CURRENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_DEEPLINK
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_WEB_PLATFORM
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
        val currentView = arguments[ARG_CURRENT]
        val deepLink = arguments[ARG_DEEPLINK]
        val isWebPlatform = arguments[ARG_WEB_PLATFORM].toBoolean()

        if(deepLink?.isNotEmpty() == true){
            systemImpl.goToDeepLink(deepLink, accountManager, context)
        }else {
            val canSelectServer = systemImpl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION,
                    context)
            val userHasLoggedInOrSelectedGuest = systemImpl.getAppPref(
                    Login2Presenter.PREFKEY_USER_LOGGED_IN, "false", context).toBoolean()

            val destination = nextViewArg ?: currentView ?: if (!userHasLoggedInOrSelectedGuest) {
                if (canSelectServer && !isWebPlatform)
                    SiteEnterLinkView.VIEW_NAME
                else
                    Login2View.VIEW_NAME
            } else {
                ContentEntryListTabsView.VIEW_NAME
            }
            val nextDestination = nextDestination(destination,isWebPlatform, arguments.toMutableMap())
            systemImpl.goToViewLink(nextDestination, context)
        }
    }

    private fun nextDestination(destination: String, isWeb:Boolean, args: MutableMap<String, String>): String {
        var nextDest = destination
        if(destination == Login2View.VIEW_NAME && isWeb && !args.containsKey(ARG_NEXT)){
            args[ARG_NEXT] = ContentEntryListTabsView.VIEW_NAME
            nextDest = destination + "?" + args.toQueryString()
        }
        return nextDest
    }
}
