package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl

import java.util.Hashtable

import com.ustadmobile.core.view.ChangePasswordView
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Presenter for ChangePassword view
 */
class ChangePasswordPresenter(context: Any,
                              arguments: Map<String, String?>,
                              view: ChangePasswordView)
    : UstadBaseController<ChangePasswordView>(context, arguments, view) {

    internal var repository: UmAppDatabase
    private val personDao: PersonDao
    private val personAuthDao: PersonAuthDao
    private var currentPerson: Person? = null
    private var currentPersonAuth: PersonAuth? = null
    private var loggedInPersonUid = 0L
    var currentPassword: String? = null
    var updatePassword: String? = null
    var updatePasswordConfirm: String? = null

    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        personDao = repository.personDao
        personAuthDao = repository.personAuthDao
        val cp = UmAccountManager.getActiveAccount(context)
        if (cp != null) {
            loggedInPersonUid = cp.personUid
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (loggedInPersonUid != 0L) {
            GlobalScope.launch {
                var result = personDao.findByUidAsync(loggedInPersonUid)
                currentPerson = result
                if (currentPerson != null) {
                    var personAuth = personAuthDao.findByUidAsync(loggedInPersonUid)
                    if (personAuth == null) {
                        currentPersonAuth = PersonAuth()
                        currentPersonAuth!!.personAuthUid = loggedInPersonUid
                    }
                }
            }
        }
    }

    fun handleClickSave() {
        if (updatePassword != null && !updatePassword!!.isEmpty() && updatePasswordConfirm != null &&
                !updatePasswordConfirm!!.isEmpty() && currentPersonAuth != null && currentPerson != null) {
            if (updatePassword != updatePasswordConfirm) {
                view.sendMessage(MessageID.passwords_dont_match)
                return
            }

            currentPersonAuth!!.passwordHash = PersonAuthDao.encryptPassword(updatePassword!!)

            GlobalScope.launch {
                personAuthDao.updateAsync(currentPersonAuth!!)

                personAuthDao.selfResetPassword(currentPerson!!.username!!, currentPassword!!,
                        updatePassword!!, loggedInPersonUid, object : UmCallback<Int> {

                    override fun onSuccess(result: Int?) {
                        GlobalScope.launch {
                            try {
                                val result = personAuthDao.updateAsync(currentPersonAuth!!)

                            }catch(e:Exception){
                                println(e!!.message)
                                view.sendMessage(MessageID.unable_to_update_password)
                            }

                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        println(exception!!.message)
                        view.sendMessage(MessageID.unable_to_update_password)
                    }

                })
            }
        }



}
