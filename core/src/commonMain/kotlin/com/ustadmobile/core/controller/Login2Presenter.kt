package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_WORKSPACE
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.WorkSpace
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import org.kodein.di.DI
import org.kodein.di.instance

class Login2Presenter(context: Any, arguments: Map<String, String>, view: Login2View,
                      di: DI)
    : UstadBaseController<Login2View>(context, arguments, view, di) {

    private  lateinit var nextDestination: String

    private lateinit var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    private val accountManager: UstadAccountManager by instance()

    private var workSpace: WorkSpace = WorkSpace().apply {
        registrationAllowed = true
        guestLogin = true
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        nextDestination = arguments[ARG_NEXT] ?: impl.getAppConfigString(
                AppConfig.KEY_FIRST_DEST, ContentEntryListTabsView.VIEW_NAME, context) ?: ContentEntryListTabsView.VIEW_NAME
        serverUrl = if (arguments.containsKey(ARG_SERVER_URL)) {
            arguments.getValue(ARG_SERVER_URL)
        } else {
            impl.getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", context)?:""
        }
        val mWorkSpace = arguments[ARG_WORKSPACE]
        if(mWorkSpace != null){
            workSpace = Json.parse(WorkSpace.serializer(), mWorkSpace)
        }
        view.createAccountVisible = workSpace.registrationAllowed
        view.connectAsGuestVisible = workSpace.guestLogin
    }

    fun handleLogin(username: String?, password:String?){
        view.inProgress = true
        view.isEmptyUsername = username == null || username.isEmpty()
        view.isEmptyPassword = password == null || password.isEmpty()

        if(username != null && username.isNotEmpty() && password != null && password.isNotEmpty()){
            GlobalScope.launch(doorMainDispatcher()) {
                try {
                    accountManager.login(username,password,serverUrl)
                    view.inProgress = false
                    impl.go(nextDestination, mapOf(),context)
                } catch (e: Exception) {
                    view.errorMessage = impl.getString(if(e is UnauthorizedException)
                        MessageID.wrong_user_pass_combo else
                        MessageID.login_network_error , context)
                    view.inProgress = false
                    view.clearFields()
                }
            }
        }else{
            view.inProgress = false
        }
    }

    fun handleCreateAccount(){
        impl.go(PersonEditView.VIEW_NAME, arguments, context)
    }

    fun handleConnectAsGuest(){
        impl.go(ContentEntryListTabsView.VIEW_NAME, arguments, context)
    }


}
