package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.lib.db.entities.UmAccount

import java.util.HashMap

class Login2Presenter(context: Any, arguments: Map<String, String>?, view: Login2View)
    : UstadBaseController<Login2View>(context, arguments!!, view) {

    private var mNextDest: String? = null

    init {
        if (arguments != null && arguments.containsKey(ARG_NEXT)) {
            mNextDest = arguments[ARG_NEXT]
        } else {
            mNextDest = UstadMobileSystemImpl.instance.getAppConfigString(
                    AppConfig.KEY_FIRST_DEST, "BasePoint", context)
        }
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        if (arguments != null && arguments!!.containsKey(ARG_SERVER_URL)) {
            view?.setServerUrl(arguments!!.getValue(ARG_SERVER_URL))
        } else {
            view?.setServerUrl(UstadMobileSystemImpl.instance.getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", getContext())!!)
        }
    }

    fun handleClickLogin(username: String, password: String, serverUrl: String) {
        view?.setInProgress(true)
        view?.setErrorMessage("")
        val loginRepoDb = UmAppDatabase.getInstance(getContext()).getRepository(serverUrl,
                "")
        val systemImpl = UstadMobileSystemImpl.instance
        loginRepoDb.personDao.login(username, password, object : UmCallback<UmAccount> {
            override fun onSuccess(result: UmAccount?) {
                if (result != null) {
                    result.endpointUrl = serverUrl
                    view?.runOnUiThread (Runnable { view?.setInProgress(false) })
                    UmAccountManager.setActiveAccount(result, getContext())
                    systemImpl.go(mNextDest, getContext())
                } else {
                    view?.runOnUiThread(Runnable  {
                        view?.setErrorMessage(systemImpl.getString(MessageID.wrong_user_pass_combo,
                                getContext()))
                        view?.setPassword("")
                        view?.setInProgress(false)
                    })
                }
            }

            override fun onFailure(exception: Throwable) {
                view?.runOnUiThread(Runnable  {
                    view?.setErrorMessage(systemImpl.getString(
                            MessageID.login_network_error, getContext()))
                    view?.setInProgress(false)
                })
            }
        })
    }

    companion object {

        val ARG_NEXT = "next"

        val ARG_SERVER_URL = "apiUrl"
    }


}
