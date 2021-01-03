package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonEditView.Companion.REGISTER_VIA_LINK
import com.ustadmobile.core.view.UstadView.Companion.ARG_FROM
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_POPUPTO_ON_FINISH
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.Site
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class Login2Presenter(context: Any, arguments: Map<String, String>, view: Login2View,
                      di: DI)
    : UstadBaseController<Login2View>(context, arguments, view, di) {

    private  lateinit var nextDestination: String

    private lateinit var fromDestination: String

    private lateinit var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    private val accountManager: UstadAccountManager by instance()

    private lateinit var workSpace: Site

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        nextDestination = arguments[ARG_NEXT] ?: impl.getAppConfigString(
                AppConfig.KEY_FIRST_DEST, ContentEntryListTabsView.VIEW_NAME, context) ?:
                ContentEntryListTabsView.VIEW_NAME

        fromDestination = if(arguments.containsKey(ARG_FROM)){
            arguments.getValue(ARG_FROM)
        }else{
            val canSelectServer = impl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION, context)
            if(canSelectServer) GetStartedView.VIEW_NAME else Login2View.VIEW_NAME
        }
        serverUrl = if (arguments.containsKey(ARG_SERVER_URL)) {
            arguments.getValue(ARG_SERVER_URL)
        } else {
            impl.getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", context)?:""
        }

        if(!serverUrl.endsWith("/")){
            serverUrl += "/"
        }
        val mWorkSpace = arguments[ARG_SITE]
        if(mWorkSpace != null){
            workSpace = safeParse(di, Site.serializer(), mWorkSpace)
        }else{
            val isRegistrationAllowed = impl.getAppConfigBoolean(AppConfig.KEY_ALLOW_REGISTRATION,
                    context)
            val isGuestLoginAllowed =  if(arguments.containsKey(Login2View.ARG_NO_GUEST)){
                false
            }else{
                impl.getAppConfigBoolean(AppConfig.KEY_ALLOW_GUEST_LOGIN,
                        context)
            }

            workSpace = Site().apply {
                registrationAllowed = isRegistrationAllowed
                guestLogin = isGuestLoginAllowed
            }

        }

        view.createAccountVisible = workSpace.registrationAllowed
        view.connectAsGuestVisible = workSpace.guestLogin
        view.versionInfo = impl.getVersion(context)
    }

    fun handleLogin(username: String?, password:String?){
        view.inProgress = true
        view.loading = true
        view.isEmptyUsername = username == null || username.isEmpty()
        view.isEmptyPassword = password == null || password.isEmpty()

        if(username != null && username.isNotEmpty() && password != null && password.isNotEmpty()){
            GlobalScope.launch(doorMainDispatcher()) {
                try {
                    val umAccount = accountManager.login(username.trim(),
                            password.trim() ,serverUrl)
                    view.inProgress = false
                    view.loading = false
                    val goOptions = UstadMobileSystemCommon.UstadGoOptions("",
                            true)
                    val accountRepo: UmAppDatabase =  di.on(umAccount).direct.instance(tag = DoorTag.TAG_REPO)
                    (accountRepo as DoorDatabaseSyncRepository).invalidateAllTables()
                    impl.go(nextDestination, mapOf(), context, goOptions)
                } catch (e: Exception) {
                    view.errorMessage = impl.getString(if(e is UnauthorizedException)
                        MessageID.wrong_user_pass_combo else
                        MessageID.login_network_error , context)
                    view.inProgress = false
                    view.loading = false
                    view.clearFields()
                }
            }
        }else{
            view.inProgress = false
            view.loading = false
        }
    }

    fun handleCreateAccount(){
        val args = mutableMapOf(
                PersonEditView.ARG_REGISTRATION_MODE to true.toString(),
                ARG_SERVER_URL to serverUrl,
                SiteTermsDetailView.ARG_SHOW_ACCEPT_BUTTON to true.toString(),
                SiteTermsDetailView.ARG_USE_DISPLAY_LOCALE to true.toString(),
                ARG_POPUPTO_ON_FINISH to (arguments[ARG_POPUPTO_ON_FINISH] ?: Login2View.VIEW_NAME))

        args.putFromOtherMapIfPresent(arguments, ARG_NEXT)
        args.putFromOtherMapIfPresent(arguments, REGISTER_VIA_LINK)

        impl.go(SiteTermsDetailView.VIEW_NAME_ACCEPT_TERMS, args, context)
    }

    fun handleConnectAsGuest(){
        accountManager.activeAccount = UmAccount(0L,"guest",
                "",serverUrl,"Guest","User")
        impl.go(ContentEntryListTabsView.VIEW_NAME, arguments, context)
    }


}
