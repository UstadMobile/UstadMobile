package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class Login2Presenter(context: Any, arguments: Map<String, String>, view: Login2View,
                      val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                      private val personRepo: PersonDao)
    : UstadBaseController<Login2View>(context, arguments, view) {

    private  var nextDestination: String? = null

    private lateinit var serverUrl: String

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

        view.createAccountVisible = impl.getAppConfigString(AppConfig.KEY_SHOW_CREATE_ACCOUNT,
                "true", context)?.toBoolean() ?: true
        view.connectAsGuestVisible = impl.getAppConfigString(AppConfig.KEY_SHOW_CONNECT_AS_GUEST,
                "true", context)?.toBoolean() ?: true
    }

    fun handleLogin(username: String?, password:String?){
        view.inProgress = true
        view.isEmptyUsername = username == null || username.isEmpty()
        view.isEmptyPassword = password == null || password.isEmpty()

        if(username != null && username.isNotEmpty() && password != null && password.isNotEmpty()){
            GlobalScope.launch {
                try {
                    defaultHttpClient().get<HttpStatement> {
                        url {
                            takeFrom(serverUrl)
                            encodedPath = "${encodedPath}Login/login"
                        }
                        parameter("username", username)
                        parameter("password", password)
                    }.execute { loginResponse ->
                        if (loginResponse.status == HttpStatusCode.OK) {
                            val account = loginResponse.receive<UmAccount>()
                            account.endpointUrl = serverUrl

                            personRepo.findByUid(account.personUid)
                            view.runOnUiThread(Runnable { view.inProgress = false })
                            UmAccountManager.setActiveAccount(account, context)

                            val activeRepository = UmAccountManager.getRepositoryForActiveAccount(context)
                            if(activeRepository is DoorDatabaseRepository) {
                                activeRepository.connectivityStatus = DoorDatabaseRepository.STATUS_CONNECTED
                            }
                            impl.go(nextDestination, context)
                        } else {
                            view.runOnUiThread(Runnable {
                                view.showSnackBar(impl.getString(MessageID.wrong_user_pass_combo,
                                        context))
                                view.clearFields()
                                view.inProgress = false
                            })
                        }
                    }
                } catch (e: Exception) {
                    view.runOnUiThread(Runnable {
                        view.showSnackBar(impl.getString(
                                MessageID.login_network_error, context))
                        view.inProgress = false
                    })
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
