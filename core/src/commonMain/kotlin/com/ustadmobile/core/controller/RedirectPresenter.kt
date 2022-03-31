package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.RedirectView.Companion.TAG_REDIRECTED
import com.ustadmobile.core.view.UstadView.Companion.ARG_DEEPLINK
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
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

        when {
            deepLink?.isNotEmpty() == true -> {
                systemImpl.goToDeepLink(deepLink, accountManager, context)
            }

            nextViewArg != null -> {
                systemImpl.goToViewLink(nextViewArg, context)
            }

            accountManager.activeSession != null -> {
                systemImpl.setAppPref(TAG_REDIRECTED, "true", context)
                systemImpl.goToViewLink(ContentEntryList2View.VIEW_NAME_HOME, context)
            }

            else -> {
                presenterScope.launch {
                    navigateToStartNewUserSession()
                }
            }
        }
    }

}
