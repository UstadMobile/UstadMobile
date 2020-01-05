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
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.lib.db.entities.PersonAuth
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
import kotlin.math.log

class LoginPresenter(context: Any, arguments: Map<String, String?>, view: LoginView,
                     val impl: UstadMobileSystemImpl)
    : UstadBaseController<LoginView>(context, arguments, view) {

    private val mNextDest: String
    private val registerCode: String
    private var personAuthdao: PersonAuthDao

    init {
        mNextDest = arguments[ARG_NEXT] ?: impl.getAppConfigString(
                AppConfig.KEY_FIRST_DEST, HomeView.VIEW_NAME, context) ?: HomeView.VIEW_NAME

        registerCode = (impl.getAppConfigString(AppConfig.KEY_SHOW_REGISTER_CODE, "", context) ?: "")
                .trim()

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

        val showRegisterLink = impl.getAppConfigString(AppConfig.KEY_SHOW_REGISTER, "false", context)?.toBoolean()
                ?: false

        view.showToolbar(false)

        view.setRegistrationLinkVisible(showRegisterLink)

        val version = impl.getVersion(context)
        view.updateVersionOnLogin(version)

        var username:String ?= null
        if (arguments.containsKey(Login2View.ARG_LOGIN_USERNAME))
        {
            username = arguments.get(Login2View.ARG_LOGIN_USERNAME).toString()
        }
        else
        {
            val impl = UstadMobileSystemImpl.instance
            username = impl.getAppPref(UmAccountManager.PREFKEY_PASSWORD_HASH_USERNAME, context)
        }
        if (username != null && !username.isEmpty())
        {
            view.updateUsername(username)
        }
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
                        view.setInProgress(false)
                        view.forceSync()
                        view.updateLastActive()
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

                if (loginResponse.status == HttpStatusCode.OK) {
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
                        view.updateLastActive()
                        view.setFinishAfficinityOnView()
                    })


                    //make sure the repository knows that it is online
                    val activeRepository = UmAccountManager.getRepositoryForActiveAccount(context)
                    if(activeRepository is DoorDatabaseRepository) {
                        activeRepository.connectivityStatus = DoorDatabaseRepository.STATUS_CONNECTED
                    }

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
                checkLocalLogin(username, password, serverUrl)
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

        const val ARG_MESSAGE = "message"
    }


}
