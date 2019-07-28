package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.Register2View
import io.ktor.util.Hash
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class LoginPresenter(context: Any, arguments: Map<String, String?>, view: LoginView, val impl: UstadMobileSystemImpl)
    : UstadBaseController<LoginView>(context, arguments, view) {

    private var mNextDest: String? = null

    init {
        mNextDest = if (arguments.containsKey(ARG_NEXT)) {
            arguments[ARG_NEXT]
        } else {
            impl.getAppConfigString(
                    AppConfig.KEY_FIRST_DEST, "BasePoint", context)
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        if (arguments.containsKey(ARG_SERVER_URL)) {
            view.setServerUrl(arguments.getValue(ARG_SERVER_URL)!!)
        } else {
            view.setServerUrl(UstadMobileSystemImpl.instance.getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", context)!!)
        }

        val showRegisterLink = impl.getAppConfigString(AppConfig.KEY_SHOW_REGISTER, "false", context)!!.toBoolean()

        view.setRegistrationLinkVisible(showRegisterLink)
    }

    @JsName("handleClickLogin")
    fun handleClickLogin(username: String, password: String, serverUrl: String) {
        view.setInProgress(true)
        view.setErrorMessage("")
        val loginRepoDb = UmAppDatabase.getInstance(context)//.getRepository(serverUrl, "")
        val systemImpl = UstadMobileSystemImpl.instance
        GlobalScope.launch {
            try {
                val result = loginRepoDb.personDao.loginAsync(username, password)
                if (result != null) {
                    result.endpointUrl = serverUrl
                    view.runOnUiThread(Runnable { view.setInProgress(false) })
                    UmAccountManager.setActiveAccount(result, context)
                    systemImpl.go(mNextDest, context)
                } else {
                    view.runOnUiThread(Runnable {
                        view.setErrorMessage(systemImpl.getString(MessageID.wrong_user_pass_combo,
                                context))
                        view.setPassword("")
                        view.setInProgress(false)
                    })
                }

            } catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.setErrorMessage(systemImpl.getString(
                            MessageID.login_network_error, context))
                    view.setInProgress(false)
                })
            }
        }
    }

    fun handleCreateAccount(){
        val args = HashMap(arguments)
        args[ARG_NEXT] = ContentEntryDetailView.VIEW_NAME
        impl.go(Register2View.VIEW_NAME,args,context)
    }

    companion object {

        const val ARG_NEXT = "next"

        const val ARG_SERVER_URL = "apiUrl"
    }


}
