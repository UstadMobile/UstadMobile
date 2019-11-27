package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.Register2View
import com.ustadmobile.lib.db.entities.Person
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class Register2Presenter(context: Any, arguments: Map<String, String?>, view: Register2View,
                         private val personDao: PersonDao)
    : UstadBaseController<Register2View>(context, arguments, view) {

    private var mNextDest: String? = null

    init {
        if (arguments.containsKey(ARG_NEXT)) {
            mNextDest = arguments[ARG_NEXT]
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (arguments.containsKey(ARG_SERVER_URL)) {
            view.setServerUrl(arguments[ARG_SERVER_URL]!!)
        } else {
            view.setServerUrl(UstadMobileSystemImpl.instance.getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", context)!!)
        }
    }

    /**
     * Registering new user's account
     * @param person Person object to be registered
     * @param password Person password to be associated with the account.
     * @param serverUrl Server url where the account should be created
     */
    @JsName("handleClickRegister")
    fun handleClickRegister(person: Person, password: String, serverUrl: String) {
        view.runOnUiThread(Runnable { view.setInProgress(true) })

        val systemImpl = UstadMobileSystemImpl.instance

        GlobalScope.launch {

            try {
                val result = personDao.registerAsync(person, password)


                if (result != null) {
                    person.personUid = result.personUid
                    personDao.createPersonAsync(person, 0)
                    result.endpointUrl = serverUrl
                    view.runOnUiThread(Runnable { view.setInProgress(false) })
                    UmAccountManager.setActiveAccount(result, context)
                    systemImpl.go(mNextDest, context)
                }
            } catch (e: Exception) {
                //TODO: Delete if created and something went wrong.
                view.runOnUiThread(Runnable {
                    view.setErrorMessageView(systemImpl.getString(MessageID.err_registering_new_user,
                            context))
                    view.setInProgress(false)
                })
            }

        }
    }

    /**
     * Registering new user's account
     * @param person Person object to be registered
     * @param password Person password to be associated with the account.
     * @param serverUrl Server url where the account should be created
     */
    fun handleClickRegister2(firstName: String, lastName:String, email:String, username:String,
                             password: String, serverUrl: String) {
        view.runOnUiThread(Runnable { view.setInProgress(true) })

        val systemImpl = UstadMobileSystemImpl.instance

        GlobalScope.launch {

            try{
                val serverUrl = UmAccountManager.getActiveEndpoint(context)
                val resetPasswordResponse = defaultHttpClient().get<HttpResponse>()
                {
                    url {
                        takeFrom(serverUrl!!)
                        encodedPath = "${encodedPath}UmAppDatabase/PersonDao/registerUser"
                    }
                    parameter("p0", firstName)
                    parameter("p1", lastName)
                    parameter("p2", email)
                    parameter("p3", username)
                    parameter("p4", password)
                    parameter("p5", serverUrl)

                }

                if (resetPasswordResponse.status == HttpStatusCode.OK) {

                    val person = Person()
                    person.firstNames = firstName
                    person.lastName = lastName
                    person.emailAddr = email
                    person.username = username

                    val result = resetPasswordResponse.readText()
                    if (result.toLong() != 0L) {
                        val personUid = result.toLong()
                        person.personUid = personUid
                        personDao.createPersonAsync(person, 0)

                        view.runOnUiThread(Runnable { view.setInProgress(false) })
                        val args = HashMap<String, String?>()
                        val userCreatedString = systemImpl.getString(
                                MessageID.created_user, context) + " : " + person.username
                        args.put(LoginPresenter.ARG_MESSAGE, userCreatedString)
                        systemImpl.go(LoginView.VIEW_NAME, args, context)
                    }

                } else {
                    println("nope")
                }
            }catch (e:Exception){
                view.runOnUiThread(Runnable {
                    view.setErrorMessageView(systemImpl.getString(
                            MessageID.err_registering_new_user,
                            context) + ", error: " + e.message)
                    view.setInProgress(false)
                })
            }

        }
    }

    companion object {

        const val ARG_NEXT = "next"

        const val ARG_SERVER_URL = "apiUrl"
    }
}
