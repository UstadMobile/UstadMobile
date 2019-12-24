package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.Register2View
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class LoginPresenter(context: Any, arguments: Map<String, String?>, view: LoginView, val impl: UstadMobileSystemImpl)
    : UstadBaseController<LoginView>(context, arguments, view) {

    private val mNextDest: String
    private val registerCode: String

    init {
        mNextDest = arguments[ARG_NEXT] ?: impl.getAppConfigString(
                AppConfig.KEY_FIRST_DEST, HomeView.VIEW_NAME, context) ?: HomeView.VIEW_NAME

        registerCode = (impl.getAppConfigString(AppConfig.KEY_SHOW_REGISTER_CODE, "", context) ?: "")
                .trim()
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        if (arguments.containsKey(ARG_SERVER_URL)) {
            view.setServerUrl(arguments.getValue(ARG_SERVER_URL)!!)
        } else {
            view.setServerUrl(UstadMobileSystemImpl.instance.getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", context)!!)
        }

        val showRegisterLink = impl.getAppConfigString(AppConfig.KEY_SHOW_REGISTER, "false", context)?.toBoolean()
                ?: false

        view.setRegistrationLinkVisible(showRegisterLink)
    }

    @JsName("handleClickLogin")
    fun handleClickLogin(username: String, password: String, serverUrl: String) {
        view.setInProgress(true)
        view.setErrorMessage("")
        GlobalScope.launch {
            try {
                val loginResponse = defaultHttpClient().get<HttpResponse>() {
                    url {
                        takeFrom(serverUrl)
                        encodedPath = "${encodedPath}Login/login"
                    }
                    parameter("username", username)
                    parameter("password", password)
                }

                if (loginResponse.status == HttpStatusCode.OK) {
                    val account = loginResponse.receive<UmAccount>()
                    account.endpointUrl = serverUrl
                    view.runOnUiThread(Runnable { view.setInProgress(false) })
                    UmAccountManager.setActiveAccount(account, context)

                    //make sure the repository knows that it is online
                    (UmAccountManager.getRepositoryForActiveAccount(context) as DoorDatabaseRepository)
                            .connectivityStatus = DoorDatabaseRepository.STATUS_CONNECTED
                    impl.go(mNextDest, context)
                } else {
                    view.runOnUiThread(Runnable {
                        view.setErrorMessage(impl.getString(MessageID.wrong_user_pass_combo,
                                context))
                        view.setPassword("")
                        view.setInProgress(false)
                    })
                }
            } catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.setErrorMessage(impl.getString(
                            MessageID.login_network_error, context))
                    view.setInProgress(false)
                })
            }
        }
    }

    @JsName("handleClickCreateAccount")
    fun handleClickCreateAccount() {
        if (registerCode.isEmpty()) {
            goToRegisterView()
        } else {
            view.showRegisterCodeDialog(
                    impl.getString(MessageID.enter_register_code, context),
                    impl.getString(MessageID.ok, context),
                    impl.getString(MessageID.cancel, context))
        }
    }

    private fun goToRegisterView() {
        impl.go(Register2View.VIEW_NAME, mapOf(ARG_NEXT to mNextDest), context)
    }

    @JsName("handleRegisterCodeDialogEntered")
    fun handleRegisterCodeDialogEntered(code: String) {
        if (code == registerCode) {
            goToRegisterView()
        } else {
            view.showSnackBarNotification(impl.getString(MessageID.invalid_register_code, context), {}, 0)
        }
    }

    companion object {

        const val ARG_NEXT = "next"

        const val ARG_SERVER_URL = "apiUrl"
    }


}
