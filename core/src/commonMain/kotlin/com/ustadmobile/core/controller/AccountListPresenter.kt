package com.ustadmobile.core.controller

import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.AccountListView.Companion.ACTIVE_ACCOUNT_MODE_HEADER
import com.ustadmobile.core.view.AccountListView.Companion.ARG_ACTIVE_ACCOUNT_MODE
import com.ustadmobile.core.view.AccountListView.Companion.ARG_FILTER_BY_ENDPOINT
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class AccountListPresenter(context: Any, arguments: Map<String, String>, view: AccountListView,
                           di: DI, val doorLifecycleOwner: DoorLifecycleOwner)
    : UstadBaseController<AccountListView>(context, arguments, view, di) {

    private val accountManager: UstadAccountManager by instance()

    private val impl: UstadMobileSystemImpl by instance()

    private var accountListLive = DoorMutableLiveData<List<UmAccount>>()

    private var endpointFilter: String? = null

    private lateinit var activeAccountMode: String

    private lateinit var nextDest: String

    private val httpClient: HttpClient by instance()

    //Removes the active account from the main list (this is normally at the top)
    private var accountListObserver : DoorObserver<List<UmAccount>> = object: DoorObserver<List<UmAccount>> {
        override fun onChanged(t: List<UmAccount>) {
            val newList = t.toMutableList()
            if(activeAccountMode == ACTIVE_ACCOUNT_MODE_HEADER) {
                val activeUserAtServer = accountManager.activeAccount.userAtServer
                newList.removeAll { it.userAtServer ==  activeUserAtServer }
            }
            accountListLive.sendValue(newList)
        }
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        endpointFilter = arguments.get(ARG_FILTER_BY_ENDPOINT)
        activeAccountMode = arguments.get(ARG_ACTIVE_ACCOUNT_MODE) ?: ACTIVE_ACCOUNT_MODE_HEADER
        view.activeAccountLive = if(activeAccountMode == ACTIVE_ACCOUNT_MODE_HEADER)
            accountManager.activeAccountLive
        else
            null

        view.accountListLive = accountListLive
        accountManager.storedAccountsLive.observe(doorLifecycleOwner, accountListObserver)
        nextDest = arguments.get(ARG_NEXT) ?: impl.getAppConfigDefaultFirstDest(context)
        }

    fun handleClickAddAccount(){
        val canSelectServer = impl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION, context)
        val args = arguments.toMutableMap().also {
            it.putIfNotAlreadySet(ARG_NEXT, nextDest)
        }

        val filterByEndpoint = arguments[ARG_FILTER_BY_ENDPOINT]
        if(filterByEndpoint != null) {
            view.loading = true
            GlobalScope.launch {
                try {
                    val site = httpClient.verifySite(filterByEndpoint)
                    val goArgs = mapOf(ARG_SERVER_URL to filterByEndpoint,
                        ARG_SITE to Json.encodeToString(Site.serializer(), site),
                        ARG_NEXT to nextDest)

                    impl.go(Login2View.VIEW_NAME, goArgs, context)
                }catch(e: Exception) {
                    Napier.e("Exception getting site object", e)
                    view.showSnackBar(impl.getString(MessageID.login_network_error, context))
                }
            }
        }else {
            impl.go(if(canSelectServer) SiteEnterLinkView.VIEW_NAME else Login2View.VIEW_NAME,args, context)
        }
    }

    fun handleClickDeleteAccount(account: UmAccount){
        accountManager.removeAccount(account)
    }

    fun handleClickProfile(personUid: Long){
        impl.go(PersonDetailView.VIEW_NAME, mapOf(ARG_ENTITY_UID to personUid.toString()),
                context)
    }

    fun handleClickAbout(){
        impl.goToViewLink(AboutView.VIEW_NAME, context)
    }

    fun handleClickLogout(account: UmAccount){
        //TODO: Fix this - the if condition can never be satisifed
        accountManager.removeAccount(account)
        if(accountManager.storedAccounts.size == 1
                && accountManager.storedAccounts.contains(account)){
            //view.showGetStarted()
        }
    }

    fun handleClickAccount(account: UmAccount){
        accountManager.activeAccount = account
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(
            arguments[UstadView.ARG_POPUPTO_ON_FINISH] ?: UstadView.ROOT_DEST,
            false)
        val snackMsg = impl.getString(MessageID.logged_in_as, context)
            .replace("%1\$s", account.username ?: "")
            .replace("%2\$s", account.endpointUrl)
        val dest = nextDest.appendQueryArgs(
            mapOf(UstadView.ARG_SNACK_MESSAGE to snackMsg).toQueryString())
        impl.goToViewLink(dest, context, goOptions)
    }
}
