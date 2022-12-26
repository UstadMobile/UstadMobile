package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.BrowserLinkOpener
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.LINK_ENDPOINT_VIEWNAME_DIVIDER
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL

/**
 * Navigate to a given viewUri
 *
 * e.g. ViewName?arg=value&arg2=value2
 */
fun UstadNavController.navigateToViewUri(
    viewUri: String,
    goOptions: UstadMobileSystemCommon.UstadGoOptions
) {
    val viewName = viewUri.substringBefore('?')
    val args = UMFileUtil.parseURLQueryString(viewUri)

    navigate(viewName, args, goOptions)
}



/**
 * Open the given link. This will handle redirecting the user to the accountlist, login, or enter
 * site link as needed.
 */
suspend fun UstadNavController.navigateToLink(
    link: String,
    accountManager: UstadAccountManager,
    browserLinkOpener: BrowserLinkOpener,
    goOptions: UstadMobileSystemCommon.UstadGoOptions = UstadMobileSystemCommon.UstadGoOptions.Default,
    forceAccountSelection: Boolean = false,
    userCanSelectServer: Boolean = true,
    accountName: String? = null,
) {
    var endpointUrl: String? = null
    var viewUri: String? = null



    when {
        link.startsWithHttpProtocol() && link.contains(LINK_ENDPOINT_VIEWNAME_DIVIDER) -> {
            val urlComponents = UstadUrlComponents.parse(link)
            endpointUrl = urlComponents.endpoint
            viewUri = urlComponents.viewUri
        }

        !link.startsWithHttpProtocol() -> {
            viewUri = link
        }
    }

    val maxDateOfBirth = if(viewUri?.startsWith(ParentalConsentManagementView.VIEW_NAME) == true) {
        (DateTime.now() - UstadMobileConstants.ADULT_AGE_THRESHOLD.years).unixMillisLong
    }else {
        0L
    }

    when {
        //when the link is not an ustad link, open in browser
        viewUri == null -> {
            browserLinkOpener.onOpenLink(link)
        }

        //When the account has already been selected and the endpoint url is known.
        accountName != null && endpointUrl != null -> {
            val session = accountManager.activeSessionsList { filterUrl -> filterUrl == endpointUrl }
                .firstOrNull {
                    it.person.username == accountName.substringBefore("@")
                }
            if(session != null) {
                accountManager.activeSession = session
                navigateToViewUri(viewUri, goOptions)
            }
        }

        //when the active account is already on the given endpoint, or there is no endpoint
        //specified, then go directly to the given view (unless the force account selection option
        //is set)
        !forceAccountSelection &&
            (endpointUrl == null || accountManager.activeEndpoint.url == endpointUrl) ->
        {
            navigateToViewUri(viewUri, goOptions)
        }

        //If the endpoint Url is known and there are no active accounts for this server,
        // go directly to login
        endpointUrl != null
            && accountManager.activeSessionCount(maxDateOfBirth) { it == endpointUrl } == 0 ||
        //When the endpoint url is not known, but there are no accounts at all, and the user cannot
        ///select a server, go directly to login
        endpointUrl == null && accountManager.activeSessionCount(maxDateOfBirth) == 0
            && !userCanSelectServer ->
        {
            val args = mutableMapOf(ARG_NEXT to viewUri)
            if(endpointUrl != null)
                args[ARG_SERVER_URL] = endpointUrl

            navigate(Login2View.VIEW_NAME, args.toMap(), goOptions)
        }
        //If there are no accounts, the endpoint url is not specified, and the user can select the server, go to EnterLink
        endpointUrl == null && accountManager.activeSessionCount(maxDateOfBirth) == 0 && userCanSelectServer -> {
            navigate(SiteEnterLinkView.VIEW_NAME, mapOf(ARG_NEXT to viewUri))
        }

        //else - go to the account manager
        else -> {
            val args = mutableMapOf(ARG_NEXT to viewUri)
            if(endpointUrl != null)
                args[AccountListView.ARG_FILTER_BY_ENDPOINT] = endpointUrl

            args[AccountListView.ARG_ACTIVE_ACCOUNT_MODE] = AccountListView.ACTIVE_ACCOUNT_MODE_INLIST
            args[UstadView.ARG_LISTMODE] = ListViewMode.PICKER.toString()
            args[UstadView.ARG_MAX_DATE_OF_BIRTH] = maxDateOfBirth.toString()

            navigate(AccountListView.VIEW_NAME, args.toMap(), goOptions)
        }
    }

}