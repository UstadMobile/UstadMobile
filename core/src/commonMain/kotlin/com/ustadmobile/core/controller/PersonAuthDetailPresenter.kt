package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.PersonAuthDetailView
import com.ustadmobile.core.view.PersonAuthDetailView.Companion.ARG_PERSONAUTH_PERSONUID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.util.encryptPassword
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
            currentPersonUid = arguments!!.get(ARG_PERSONAUTH_PERSONUID)!!.toLong()
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        if (currentPersonUid != 0L) {
            GlobalScope.launch {
                val result = personDao.findByUidAsync(currentPersonUid)
                currentPerson = result
                usernameSet = currentPerson!!.username
                if (usernameSet != null) {
                    view.updateUsername(usernameSet!!)
                }

                val result2 = personAuthDao.findByUidAsync(currentPersonUid)
                currentPersonAuth = result2
                if (result == null) {
                    currentPersonAuth = PersonAuth()
                    currentPersonAuth!!.personAuthUid = currentPersonUid
                    currentPersonAuth!!.personAuthStatus = (PersonAuth.STATUS_NOT_SENT)
                }

            }
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

            currentPersonAuth!!.passwordHash = encryptPassword(passwordSet!!)
            currentPersonAuth!!.personAuthStatus = (PersonAuth.STATUS_NOT_SENT)
            GlobalScope.launch {
                personDao.updateAsync(currentPerson!!)


                //TODO: KMP: Reset password when Ready ?
                val result = personAuthDao.updateAsync(currentPersonAuth!!)
                personAuthDao.resetPassword(currentPersonUid, passwordSet, loggedInPersonUid)
                personAuthDao.updateAsync(currentPersonAuth!!)
                view.finish()


//                override fun onFailure(exception: Throwable?) {
//                    print(exception!!.message)
//                    view.sendMessage(MessageID.unable_to_update_password)
//                }
            }
        }
    }
}
