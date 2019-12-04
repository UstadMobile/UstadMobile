package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.Register2View
import com.ustadmobile.lib.db.entities.PersonAuth
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

class LoginPresenter(context: Any, arguments: Map<String, String?>, view: LoginView,
                     val impl: UstadMobileSystemImpl)
    : UstadBaseController<LoginView>(context, arguments, view) {

    private var mNextDest: String? = null
    private var personAuthdao: PersonAuthDao

    init {
        mNextDest = if (arguments.containsKey(ARG_NEXT)) {
            arguments[ARG_NEXT]
        } else {
            impl.getAppConfigString(
                    AppConfig.KEY_FIRST_DEST, "BasePoint", context)
        }

        personAuthdao = UmAppDatabase.getInstance(context!!).personAuthDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        if (arguments.containsKey(ARG_SERVER_URL)) {
            view.setServerUrl(arguments.getValue(ARG_SERVER_URL)!!)
        } else {
            view.setServerUrl(UstadMobileSystemImpl.instance.getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", context)!!)
        }
        if(arguments.containsKey(ARG_MESSAGE)){
            view.setMessage(arguments.get(ARG_MESSAGE)!!)
        }

        val showRegisterLink = impl.getAppConfigString(AppConfig.KEY_SHOW_REGISTER,
                "false", context)!!.toBoolean()

        view.setRegistrationLinkVisible(showRegisterLink)

        val version = impl.getVersion(context)
        view.updateVersionOnLogin(version)
    }

    private fun checkLocalLogin(username:String, password:String, serverUrl: String){

        GlobalScope.launch {
            val time = UMCalendarUtil.getDateInMilliPlusDays(0)
            val person = personAuthdao.findPersonByUsername(username)
            val account = personAuthdao.authenticate(username, password)
            when {
                person == null -> view.runOnUiThread(Runnable {
                    view.setErrorMessage(impl.getString(
                            MessageID.login_no_network_no_person, context))
                    view.setInProgress(false)
                })
                account != null -> {
                    account.endpointUrl = serverUrl
                    view.runOnUiThread(Runnable { view.setInProgress(false) })
                    UmAccountManager.setActiveAccount(account, context)
                    view.runOnUiThread(Runnable {
                        view.forceSync()
                        view.setFinishAfficinityOnView()
                    })

                    impl.go(mNextDest, context)
                }
                else -> view.runOnUiThread(Runnable {
                    view.setErrorMessage(impl.getString(
                            MessageID.login_no_network_auth_fail, context))
                    view.setInProgress(false)
                })
            }
        }
    }

    @JsName("handleClickLogin")
    fun handleClickLogin(username: String, password: String, serverUrl: String) {
        view.setInProgress(true)
        val usernameTrim = username.trim()
        view.setErrorMessage("")
        GlobalScope.launch {
            try {
                val loginResponse = defaultHttpClient().get<HttpResponse>() {
                    url {
                        takeFrom(serverUrl)
                        encodedPath = "${encodedPath}Login/login"
                    }
                    parameter("username", usernameTrim)
                    parameter("password", password)
                }

                if(loginResponse.status == HttpStatusCode.OK) {
                    val account = loginResponse.receive<UmAccount>()
                    account.endpointUrl = serverUrl

                    val passwordHash = PersonAuthDao.Companion.encryptThisPassword(password)
                    val person = personAuthdao.findPersonByUsername(username)
                    if(person!=null){
                        //Persist local PersonAuth
                        var personAuth = personAuthdao.findByUid(person.personUid)
                        if(personAuth== null){
                            personAuth = PersonAuth(person.personUid, passwordHash)
                            personAuthdao.insert(personAuth)
                        }
                        personAuth.passwordHash = passwordHash

                        personAuthdao.update(personAuth)

                    }

                    UmAccountManager.setActiveAccount(account, context)
                    view.runOnUiThread(Runnable {
                        view.setInProgress(false)
                        view.forceSync()
                        view.setFinishAfficinityOnView()
                    })

                    impl.go(mNextDest, context)
                }else {
                    view.runOnUiThread(Runnable {
                        view.setErrorMessage(impl.getString(MessageID.wrong_user_pass_combo,
                                context))
                        view.setPassword("")
                        view.setInProgress(false)
                    })
                }
            } catch (e: Exception) {
                checkLocalLogin(username, password, serverUrl)
            }
        }
    }

    @JsName("handleCreateAccount")
    fun handleCreateAccount(){
        val args = HashMap(arguments)
        args[ARG_NEXT] = ContentEntryDetailView.VIEW_NAME
        impl.go(Register2View.VIEW_NAME,args,context)
    }

    companion object {

        const val ARG_NEXT = "next"

        const val ARG_SERVER_URL = "apiUrl"

        const val ARG_MESSAGE = "message"
    }


}
