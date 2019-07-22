package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao.Companion.ENCRYPTED_PASS_PREFIX
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager.PREFKEY_PASSWORD_HASH_USERNAME
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.Login2View.Companion.ARG_LOGIN_USERNAME
import com.ustadmobile.lib.util.encryptPassword
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.UmAccount

class Login2Presenter(context: Any, arguments: Map<String, String?>, view: Login2View)
    : UstadBaseController<Login2View>(context, arguments, view) {

    private var mNextDest: String? = null

    init {
        if (arguments.containsKey(ARG_NEXT)) {
            mNextDest = arguments[ARG_NEXT]
        } else {
            mNextDest = UstadMobileSystemImpl.instance.getAppConfigString(
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

        var username:String ?= null
        if (arguments.containsKey(ARG_LOGIN_USERNAME))
        {
            username = arguments.get(ARG_LOGIN_USERNAME).toString()
        }
        else
        {
            val impl = UstadMobileSystemImpl.instance
            username = impl.getAppPref(PREFKEY_PASSWORD_HASH_USERNAME, context)
        }
        if (username != null && !username.isEmpty())
        {
            view.updateUsername(username)
        }
    }

    @JsName("handleClickLogin")
    fun handleClickLogin(username: String, password: String, serverUrl: String,
                         saveToFingerprint:Boolean) {
        view.setInProgress(true)
        view.setErrorMessage("")
        val loginRepoDb = UmAppDatabase.getInstance(context)//.getRepository(serverUrl, "")
        val systemImpl = UstadMobileSystemImpl.instance
        //Update password hash to impl
        val passwordHash:String = ENCRYPTED_PASS_PREFIX + encryptPassword(password)
        GlobalScope.launch {
            try {
                val result = loginRepoDb.personDao.loginAsync(username, password)
                if (result != null) {
                    if(saveToFingerprint) {
                        UmAccountManager.setFingerprintPersonId(result.personUid, context,
                                systemImpl)
                        UmAccountManager.setFingerprintUsername(result.username, context,
                                systemImpl)
                        UmAccountManager.setFringerprintAuth(result.auth, context,
                                systemImpl)

                    }
                    UmAccountManager.updateCredCache(username, result.personUid,
                            passwordHash, context, systemImpl)
                    loginOK(result, serverUrl)

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


                    //Try local login:
                    if(UmAccountManager.checkCredCache(username, passwordHash, context, systemImpl)){
                        loginOKFromOtherSource(serverUrl,
                                UmAccountManager.getCachedPersonUid(context, systemImpl),
                                username, passwordHash)
                    }else {
                        view.setErrorMessage(systemImpl.getString(
                                MessageID.login_network_error, context ))
                        view.setInProgress(false)
                    }

                })
            }
        }
    }

    fun loginOK(result: UmAccount, serverUrl:String){
        val systemImpl = UstadMobileSystemImpl.instance
        if(serverUrl != null && !serverUrl.isEmpty()){
            result.endpointUrl = serverUrl
        }
        view.runOnUiThread(Runnable { view.setInProgress(false) })
        UmAccountManager.setActiveAccount(result, context)
        view.forceSync()
        view.updateLastActive()
        view.setFinishAfficinityOnView()
        systemImpl.go(mNextDest, context)
    }

    fun loginOKFromOtherSource(serverUrl: String, auth: String) {
        val impl = UstadMobileSystemImpl.instance
        val result = UmAccount(
                (UmAccountManager.getFingerprintPersonId(context, impl)!!).toLong() ,
                UmAccountManager.getFingerprintUsername(context, impl),
                auth, serverUrl)
        loginOK(result, serverUrl)
    }

    private fun loginOKFromOtherSource(serverUrl: String, personUid: Long?, username: String,
                                       auth: String) {
        val result = UmAccount(personUid!!, username, auth, serverUrl)
        loginOK(result, serverUrl)
    }


    fun handleClickFingerPrint() {

    }

    companion object {

        const val ARG_NEXT = "next"

        const val ARG_SERVER_URL = "apiUrl"
    }


}
