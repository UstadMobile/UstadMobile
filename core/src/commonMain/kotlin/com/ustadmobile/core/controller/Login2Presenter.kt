package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.lib.db.entities.UmAccount

import com.ustadmobile.core.view.Login2View.Companion.ARG_STARTSYNCING

class Login2Presenter(context: Any, arguments: Map<String, String>?, view: Login2View,
                      val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<Login2View>(context, arguments!!, view) {

    private var mNextDest: String? = null

    init {
        if (arguments != null && arguments!!.containsKey(ARG_NEXT)) {
            mNextDest = arguments!!.get(ARG_NEXT)!!.toString()
        } else {
            mNextDest = impl.getAppConfigString(
                    AppConfig.KEY_FIRST_DEST, "BasePoint", context)
        }
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)


        if (arguments != null && arguments!!.containsKey(ARG_SERVER_URL)) {
            view.setServerUrl(arguments[ARG_SERVER_URL]!!)
        } else {
            view.setServerUrl(impl.getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", context))
        }

        val version = impl.getVersion(context)
        view.updateVersionOnLogin(version)
    }

    /**
     * Handles login. If successful, will go to the next main destination. If not it will show
     * an error message on the view.
     * @param username  Username in plain text
     * @param password  Password in plain text
     * @param serverUrl Server url in plain text
     */
    fun handleClickLogin(username: String, password: String, serverUrl: String) {
        view.setInProgress(true)
        view.setErrorMessage("")
        val loginRepoDb = UmAppDatabase.getInstance(context).getRepository(serverUrl,
                "")
        loginRepoDb.personDao.login(username, password, object : UmCallback<UmAccount> {
            override fun onSuccess(result: UmAccount?) {
                if (result != null) {
                    result.endpointUrl = serverUrl
                    view.runOnUiThread({ view.setInProgress(false) })
                    view.setFinishAfficinityOnView()
                    UmAccountManager.setActiveAccount(result, context)

                    view.forceSync()
                    val args = Hashtable<String, String>()
                    args.put(ARG_STARTSYNCING, "true")
                    impl.go(mNextDest, args, context)
                } else {
                    view.runOnUiThread({
                        view.setErrorMessage(systemImpl.getString(MessageID.wrong_user_pass_combo,
                                context))
                        view.setPassword("")
                        view.setInProgress(false)
                    })
                }
            }

            override fun onFailure(exception: Throwable?) {
                view.runOnUiThread({
                    view.setErrorMessage(impl.getString(
                            MessageID.login_network_error, context))
                    view.setInProgress(false)
                })
            }
        })
    }

    companion object {

        private val ARG_NEXT = "next"

        val ARG_SERVER_URL = "apiUrl"
    }


}
