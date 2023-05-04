package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.RedirectView.Companion.TAG_REDIRECTED
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_OPEN_LINK
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class RedirectPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: RedirectView,
    di: DI
): UstadBaseController<RedirectView>(context, arguments, view, di) {

    val systemImpl: UstadMobileSystemImpl by instance()

    val accountManager: UstadAccountManager by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val nextViewArg = arguments[ARG_NEXT]
        val deepLink = arguments[ARG_OPEN_LINK]

        when {
            deepLink?.isNotEmpty() == true -> {
                Napier.d { "Redirect: Go to deep link: $deepLink" }
                presenterScope.launch {
                    ustadNavController?.navigateToLink(deepLink, accountManager, {  },
                        forceAccountSelection = true,
                        accountName = arguments[UstadView.ARG_ACCOUNT_NAME])
                }
            }

            nextViewArg != null -> {
                Napier.d { "Redirect: Go to nextViewArg: $nextViewArg" }
                presenterScope.launch {
                    ustadNavController?.navigateToLink(nextViewArg, accountManager, { })
                }
            }

            //There is an active, unlocked session
            accountManager.activeSession?.userSession?.locked == false -> {
                Napier.d { "Redirect: go to ${ContentEntryList2View.VIEW_NAME_HOME}" }
                systemImpl.setAppPref(TAG_REDIRECTED, "true", context)
                ustadNavController?.navigate(ContentEntryList2View.VIEW_NAME_HOME, mapOf())
            }

            else -> {
                Napier.d { "Redirect: go to start new user session" }
                presenterScope.launch {
                    navigateToStartNewUserSession()
                }
            }
        }
    }

}
