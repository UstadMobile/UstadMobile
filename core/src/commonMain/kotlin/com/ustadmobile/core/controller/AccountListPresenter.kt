package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.putIfNotAlreadySet
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI
import org.kodein.di.instance

class AccountListPresenter(context: Any, arguments: Map<String, String>, view: AccountListView,
                           di: DI)
    : UstadBaseController<AccountListView>(context, arguments, view, di) {

    val accountManager: UstadAccountManager by instance()

    val impl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.accountListLive = accountManager.storedAccountsLive
        view.activeAccountLive = accountManager.activeAccountLive
    }

    fun handleClickAddAccount(){
        val canSelectServer = impl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION, context)
        val args = arguments.toMutableMap().also {
            val nextDest = impl.getAppConfigString(AppConfig.KEY_FIRST_DEST, null, context)
                ?: ContentEntryListTabsView.VIEW_NAME
            it.putIfNotAlreadySet(ARG_NEXT, nextDest)
        }

        impl.go(if(canSelectServer) SiteEnterLinkView.VIEW_NAME else Login2View.VIEW_NAME,args, context)
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
        val dest = impl.getAppConfigDefaultFirstDest(context)
            .appendQueryArgs(mapOf(UstadView.ARG_SNACK_MESSAGE to snackMsg).toQueryString())
        impl.goToViewLink(dest, context, goOptions)
    }
}
