package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UmPlatformUtil
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.putIfNotAlreadySet
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.AccountListView.Companion.ACTIVE_ACCOUNT_MODE_HEADER
import com.ustadmobile.core.view.AccountListView.Companion.ARG_ACTIVE_ACCOUNT_MODE
import com.ustadmobile.core.view.AccountListView.Companion.ARG_FILTER_BY_ENDPOINT
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_INTENT_MESSAGE
import com.ustadmobile.core.view.UstadView.Companion.ARG_MAX_DATE_OF_BIRTH
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_TITLE
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.DoorMediatorLiveData
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class AccountListPresenter(context: Any, arguments: Map<String, String>, view: AccountListView,
                           di: DI, val doorLifecycleOwner: LifecycleOwner)
    : UstadBaseController<AccountListView>(context, arguments, view, di, activeSessionRequired = false) {

    private val accountManager: UstadAccountManager by instance()

    private val impl: UstadMobileSystemImpl by instance()

    private var endpointFilter: String? = null

    private lateinit var activeAccountMode: String

    private lateinit var nextDest: String

    //Removes the active account from the main list (this is normally at the top)
    private val accountListMediator = DoorMediatorLiveData<List<UserSessionWithPersonAndEndpoint>>()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        endpointFilter = arguments[ARG_FILTER_BY_ENDPOINT]
        activeAccountMode = arguments[ARG_ACTIVE_ACCOUNT_MODE] ?: ACTIVE_ACCOUNT_MODE_HEADER
        view.activeAccountLive = if(activeAccountMode == ACTIVE_ACCOUNT_MODE_HEADER)
            accountManager.activeUserSessionLive
        else
            null

        view.accountListLive = accountListMediator

        accountListMediator.addSource(accountManager.activeUserSessionsLive) { sessionList ->
            val newList = sessionList.toMutableList()
            if(activeAccountMode == ACTIVE_ACCOUNT_MODE_HEADER)
                newList.removeAll { it.userSession.usUid == accountManager.activeSession?.userSession?.usUid }

            if(endpointFilter != null) {
                newList.removeAll { it.endpoint.url != endpointFilter }
            }

            arguments[ARG_MAX_DATE_OF_BIRTH]?.also { maxDateOfBirthStr ->
                val maxDateOfBirth = maxDateOfBirthStr.toLong()
                newList.removeAll { it.person.dateOfBirth > maxDateOfBirth }
            }

            accountListMediator.postValue(newList)
        }

        nextDest = arguments[ARG_NEXT] ?: impl.getAppConfigDefaultFirstDest(context)
        view.intentMessage = arguments[ARG_INTENT_MESSAGE]
        view.title = arguments[ARG_TITLE] ?: impl.getString(MessageID.accounts, context)
    }

    fun handleClickAddAccount(){
        val filterByEndpoint = arguments[ARG_FILTER_BY_ENDPOINT]
        if(filterByEndpoint != null) {
            val args = mapOf(
                ARG_SERVER_URL to filterByEndpoint,
                ARG_NEXT to nextDest,
                ARG_MAX_DATE_OF_BIRTH to (arguments[ARG_MAX_DATE_OF_BIRTH] ?: "0")
            )
            impl.go(Login2View.VIEW_NAME, args, context)
        }else {
            val canSelectServer = if(UmPlatformUtil.isWeb) false else impl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION, context)
            val args = arguments.toMutableMap().also {
                it.putIfNotAlreadySet(ARG_NEXT, nextDest)
            }
            impl.go(if(canSelectServer) SiteEnterLinkView.VIEW_NAME else Login2View.VIEW_NAME, args,
                context)
        }
    }

    fun handleClickDeleteSession(session: UserSessionWithPersonAndEndpoint){
        presenterScope.launch {
            accountManager.endSession(session)
        }
    }

    fun handleClickProfile(personUid: Long){
        impl.go(PersonDetailView.VIEW_NAME, mapOf(ARG_ENTITY_UID to personUid.toString()),
                context)
    }

    fun handleClickAbout(){
        impl.goToViewLink(AboutView.VIEW_NAME, context)
    }

    fun handleClickLogout(session: UserSessionWithPersonAndEndpoint){
        presenterScope.launch {
            accountManager.endSession(session)
            navigateToStartNewUserSession()
        }
    }

    fun handleClickUserSession(session: UserSessionWithPersonAndEndpoint){
        accountManager.activeSession = session
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(
            arguments[UstadView.ARG_POPUPTO_ON_FINISH] ?: UstadView.ROOT_DEST,
            false)
        val snackMsg = impl.getString(MessageID.logged_in_as, context)
            .replace("%1\$s", session.person.username ?: "")
            .replace("%2\$s", session.endpoint.url)
        val dest = nextDest.appendQueryArgs(
            mapOf(UstadView.ARG_SNACK_MESSAGE to snackMsg).toQueryString())
        impl.goToViewLink(dest, context, goOptions)
    }
}
