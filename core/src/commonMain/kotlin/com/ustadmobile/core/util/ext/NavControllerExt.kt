package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase.Companion.LinkTarget
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.util.UstadUrlComponents.Companion.DEFAULT_DIVIDER
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNINGSPACE_URL
import com.ustadmobile.core.viewmodel.AddAccountSelectNewOrExistingViewModel
import com.ustadmobile.core.viewmodel.UstadViewModel.Companion.ARG_DONT_SET_CURRENT_SESSION
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementViewModel
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
/**
 * Navigate to a given viewUri
 *
 * e.g. ViewName?arg=value&arg2=value2
 */
fun UstadNavController.navigateToViewUri(
    viewUri: String,
    goOptions: UstadMobileSystemCommon.UstadGoOptions
) {
    val questionIndex = viewUri.indexOf('?')
    val viewName = if(questionIndex != -1) viewUri.substring(0, questionIndex) else viewUri
    val args = if(questionIndex > 0) {
        UMFileUtil.parseURLQueryString(viewUri.substring(questionIndex))
    }else {
        emptyMap()
    }

    navigate(viewName, args, goOptions)
}



/**
 * Open the given link. This will handle redirecting the user to the accountlist, login, or enter
 * site link as needed.
 *
 * Note: if we are opening an external link, this must be done synchronously. On Javascript opening
 * tabs is only allowed in response to events.
 *
 * @param dontSetCurrentSession Set UstadViewModel.ARG_DONT_SET_CURRENT_SESSION when navigating to
 *        Login, AccountList, etc.
 *
 * @return If the link is internal, then opening the link will be done asynchronously (required to
 * check existing accounts etc) a Job will be returned. If the link is external, it is opened
 * synchronously and null will be returned.
 */
@OptIn(DelicateCoroutinesApi::class)
fun UstadNavController.navigateToLink(
    link: String,
    accountManager: UstadAccountManager,
    openExternalLinkUseCase: OpenExternalLinkUseCase,
    goOptions: UstadMobileSystemCommon.UstadGoOptions = UstadMobileSystemCommon.UstadGoOptions.Default,
    forceAccountSelection: Boolean = false,
    userCanSelectServer: Boolean = true,
    accountName: String? = null,
    scope: CoroutineScope = GlobalScope,
    linkTarget: LinkTarget = LinkTarget.DEFAULT,
    dontSetCurrentSession: Boolean = false,
) : Job? {
    var learningSpaceUrl: String? = null
    var viewUri: String? = null


    when {
        link.startsWithHttpProtocol() && link.contains(DEFAULT_DIVIDER) -> {
            val urlComponents = UstadUrlComponents.parse(link)
            learningSpaceUrl = urlComponents.learningSpace
            viewUri = urlComponents.viewUri
        }

        !link.startsWithHttpProtocol() -> {
            viewUri = link
        }
    }

    val maxDateOfBirth = if(viewUri?.startsWith(ParentalConsentManagementViewModel.DEST_NAME) == true) {
        Clock.System.now().minus(UstadMobileConstants.ADULT_AGE_THRESHOLD, DateTimeUnit.YEAR, TimeZone.UTC)
            .toEpochMilliseconds()
    }else {
        0L
    }

    /**
     * Where the link is not an Ustad link, or the link is an ustad link but the system does not
     * allow the user to select to connect to another server, then we need to open the link in a
     * via openExternalLinkUseCase
     */
    return if(viewUri == null ||
        !userCanSelectServer && learningSpaceUrl != null && learningSpaceUrl != accountManager.activeLearningSpace.url
    ) {
        //when the link is not an ustad link, open in browser
        openExternalLinkUseCase(link, linkTarget)
        null
    }else {
        scope.launch {
            when {
                //When the account has already been selected and the learning space url is known.
                accountName != null && learningSpaceUrl != null -> {
                    val session = accountManager.activeSessionsList { filterUrl ->
                        filterUrl == learningSpaceUrl
                    }.firstOrNull {
                        it.person.username == accountName.substringBefore("@")
                    }
                    if(session != null) {
                        accountManager.takeIf { !dontSetCurrentSession }?.currentUserSession = session
                        navigateToViewUri(viewUri, goOptions)
                    }
                }

                //when the current account is already on the given learning space, or there is no learning space
                //specified, then go directly to the destination viewUri (unless the force account selection option
                //is set)
                !forceAccountSelection
                        && !accountManager.currentUserSession.userSession.isTemporary()
                        && (learningSpaceUrl == null || accountManager.activeLearningSpace.url == learningSpaceUrl) ->
                {
                    navigateToViewUri(viewUri, goOptions)
                }

                //If the learning space Url is known and there are no active accounts for that
                // learning space, go to new or existing account selector, and set the learning space
                // url argument.
                (learningSpaceUrl != null
                        && accountManager.activeSessionCount(maxDateOfBirth) { it == learningSpaceUrl } == 0 ) ||
                //When the learning space url is not known, but there are no accounts at all, go to
                // new or existing account selector screen
                (learningSpaceUrl == null && accountManager.activeSessionCount(maxDateOfBirth) == 0) -> {
                    val args = mutableMapOf(
                        ARG_NEXT to viewUri,
                        ARG_DONT_SET_CURRENT_SESSION to dontSetCurrentSession.toString(),
                    )

                    if(learningSpaceUrl != null)
                        args[ARG_LEARNINGSPACE_URL] = learningSpaceUrl

                    navigate(AddAccountSelectNewOrExistingViewModel.DEST_NAME, args.toMap(), goOptions)
                }

                //else - go to the account manager
                else -> {
                    val args = mutableMapOf(
                        ARG_NEXT to viewUri,
                        ARG_DONT_SET_CURRENT_SESSION to dontSetCurrentSession.toString(),
                    )

                    if(learningSpaceUrl != null)
                        args[AccountListViewModel.ARG_FILTER_BY_LEARNINGSPACE] = learningSpaceUrl

                    args[AccountListViewModel.ARG_ACTIVE_ACCOUNT_MODE] = AccountListViewModel.ACTIVE_ACCOUNT_MODE_INLIST
                    args[UstadView.ARG_LISTMODE] = ListViewMode.PICKER.toString()
                    args[UstadView.ARG_MAX_DATE_OF_BIRTH] = maxDateOfBirth.toString()

                    navigate(AccountListViewModel.DEST_NAME, args.toMap(), goOptions)
                }
            }
        }
    }
}