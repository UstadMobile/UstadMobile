package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.Register2View
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount

import java.util.HashMap

class Register2Presenter(context: Any, arguments: Map<String, String>, view: Register2View) : UstadBaseController<Register2View>(context, arguments, view) {

    private var mNextDest: String? = null

    private var umAppDatabase: UmAppDatabase? = null

    private var repo: UmAppDatabase? = null

    init {
        if (arguments.containsKey(ARG_NEXT)) {
            mNextDest = arguments[ARG_NEXT]
        }
    }

    override fun onCreate(savedState: Map<String, String>) {
        super.onCreate(savedState)

        if (arguments.containsKey(ARG_SERVER_URL)) {
            view.setServerUrl(arguments[ARG_SERVER_URL])
        } else {
            view.setServerUrl(UstadMobileSystemImpl.getInstance().getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", getContext()))
        }
    }

    //only for testing
    fun setClientDb(database: UmAppDatabase) {
        this.umAppDatabase = database
    }

    fun setRepo(repo: UmAppDatabase) {
        this.repo = repo
    }

    /**
     * Registering new user's account
     * @param person Person object to be registered
     * @param password Person password to be associated with the account.
     * @param serverUrl Server url where the account should be created
     */
    fun handleClickRegister(person: Person, password: String, serverUrl: String) {
        view.runOnUiThread { view.setInProgress(true) }

        val systemImpl = UstadMobileSystemImpl.getInstance()
        if (umAppDatabase == null) {
            umAppDatabase = UmAppDatabase.getInstance(getContext()).getRepository(serverUrl,
                    "")
        }

        if (repo == null) {
            repo = UmAccountManager.getRepositoryForActiveAccount(getContext())
        }

        repo!!.personDao
                .register(person, password, object : UmCallback<UmAccount> {
                    override fun onSuccess(result: UmAccount?) {
                        if (result != null) {
                            person.personUid = result.personUid
                            umAppDatabase!!.personDao.insertAsync(person, object : UmCallback<Long> {
                                override fun onSuccess(personUid: Long?) {
                                    result.endpointUrl = serverUrl
                                    view.runOnUiThread { view.setInProgress(false) }
                                    UmAccountManager.setActiveAccount(result, getContext())
                                    systemImpl.go(mNextDest, getContext())
                                }

                                override fun onFailure(exception: Throwable) {
                                    //simple insert - this should not happen
                                    view.runOnUiThread {
                                        view.setErrorMessageView(systemImpl.getString(
                                                MessageID.err_registering_new_user, getContext()))
                                    }
                                }
                            })

                        } else {
                            view.runOnUiThread {
                                view.setErrorMessageView(systemImpl.getString(MessageID.err_registering_new_user,
                                        getContext()))
                                view.setInProgress(false)
                            }
                        }
                    }

                    override fun onFailure(exception: Throwable) {
                        view.runOnUiThread {
                            view.setInProgress(false)
                            view.setErrorMessageView(systemImpl.getString(
                                    MessageID.login_network_error, getContext()))
                        }
                    }
                })
    }

    companion object {

        val ARG_NEXT = "next"

        val ARG_SERVER_URL = "apiUrl"
    }
}
