package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl



import com.ustadmobile.core.view.PersonAuthDetailView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth

import com.ustadmobile.core.view.PersonAuthDetailView.Companion.ARG_PERSONAUTH_PERSONUID


/**
 * Presenter for PersonAuthDetail view
 */
class PersonAuthDetailPresenter(context: Any, arguments: Map<String, String>?, view:
PersonAuthDetailView) : UstadBaseController<PersonAuthDetailView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase
    private var currentPersonUid: Long = 0
    private val personDao: PersonDao
    private val personAuthDao: PersonAuthDao
    var passwordSet: String? = null
    var confirmPasswordSet: String? = null
    private var currentPerson: Person? = null
    private var currentPersonAuth: PersonAuth? = null
    var usernameSet: String? = null
    private var loggedInPersonUid = 0L

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        personDao = repository.personDao
        personAuthDao = repository.personAuthDao
        if (arguments!!.containsKey(ARG_PERSONAUTH_PERSONUID)) {
            currentPersonUid = arguments!!.get(ARG_PERSONAUTH_PERSONUID)
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        if (currentPersonUid != 0L) {
            personDao.findByUidAsync(currentPersonUid, object : UmCallback<Person> {
                override fun onSuccess(result: Person?) {
                    currentPerson = result
                    usernameSet = currentPerson!!.username
                    if (usernameSet != null) {
                        view.updateUsername(usernameSet!!)
                    }

                    personAuthDao.findByUidAsync(currentPersonUid, object : UmCallback<PersonAuth> {
                        override fun onSuccess(result: PersonAuth?) {
                            currentPersonAuth = result
                            if (result == null) {
                                currentPersonAuth = PersonAuth()
                                currentPersonAuth!!.personAuthUid = currentPersonUid
                                currentPersonAuth!!.setPersonAuthStatus(PersonAuth.STATUS_NOT_SENT)
                            }
                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        }
    }


    fun handleClickDone() {
        if (passwordSet != null && !passwordSet!!.isEmpty() && usernameSet != null
                && !usernameSet!!.isEmpty() && currentPersonAuth != null && currentPerson != null) {
            if (passwordSet != confirmPasswordSet) {
                view.sendMessage(MessageID.passwords_dont_match)
                return
            }
            currentPerson!!.username = usernameSet
            currentPersonAuth!!.passwordHash = PersonAuthDao.encryptPassword(passwordSet)
            currentPersonAuth!!.setPersonAuthStatus(PersonAuth.STATUS_NOT_SENT)
            personDao.updateAsync(currentPerson, null)

            personAuthDao.updateAsync(currentPersonAuth!!, object : UmCallback<Int> {
                override fun onSuccess(result: Int?) {
                    personAuthDao.resetPassword(currentPersonUid, passwordSet,
                            loggedInPersonUid, object : UmCallback<Int> {
                        override fun onSuccess(result: Int?) {
                            personAuthDao.updateAsync(currentPersonAuth!!, object : UmCallback<Int> {
                                override fun onSuccess(result: Int?) {
                                    view.finish()
                                }

                                override fun onFailure(exception: Throwable?) {
                                    print(exception!!.message)
                                    view.sendMessage(MessageID.unable_to_update_password)
                                }
                            })
                        }

                        override fun onFailure(exception: Throwable?) {
                            view.sendMessage(MessageID.unable_to_update_password)
                        }
                    })
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                    view.sendMessage(MessageID.unable_to_update_password)
                }
            })
        }

    }
}
