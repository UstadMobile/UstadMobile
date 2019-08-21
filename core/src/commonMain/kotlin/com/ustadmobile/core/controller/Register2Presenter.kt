package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.Register2View
import com.ustadmobile.lib.db.entities.Person
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
                    personDao.insertAsync(person)
                    result.endpointUrl = serverUrl
                    view.runOnUiThread(Runnable { view.setInProgress(false) })
                    UmAccountManager.setActiveAccount(result, context)
                    systemImpl.go(mNextDest, context)
                }
            } catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.setErrorMessageView(systemImpl.getString(MessageID.err_registering_new_user,
                            context))
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
