package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.db.dao.PersonAuthDao.Companion.ENCRYPTED_PASS_PREFIX
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.PersonAuthDetailView
import com.ustadmobile.core.view.PersonAuthDetailView.Companion.ARG_PERSONAUTH_PERSONUID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.util.encryptPassword
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
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

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        if (currentPersonUid != 0L) {
            GlobalScope.launch {
                val result = personDao.findByUidAsync(currentPersonUid)
                currentPerson = result
                usernameSet = currentPerson!!.username
                if (usernameSet != null) {
                    view.runOnUiThread(Runnable {
                        view.updateUsername(usernameSet!!)
                    })
                }

                val result2 = personAuthDao.findByUidAsync(currentPersonUid)
                currentPersonAuth = result2
                if (result2 == null) {
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

            currentPersonAuth!!.passwordHash = PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                    encryptPassword(passwordSet!!)
            currentPersonAuth!!.personAuthStatus = (PersonAuth.STATUS_NOT_SENT)
            GlobalScope.launch {
                //Update locally
                personDao.updateAsync(currentPerson!!)

                //Update on server
                try {
                    val serverUrl = UmAccountManager.getActiveEndpoint(context)
                    val resetPasswordResponse = defaultHttpClient().get<HttpResponse>()
                    {
                        url {
                            takeFrom(serverUrl!!)
                            encodedPath = "${encodedPath}UmAppDatabase/PersonAuthDao/resetPassword"
                        }
                        parameter("p0", currentPersonUid)
                        parameter("p1", passwordSet)
                        parameter("p2", loggedInPersonUid)
                    }

                    if(resetPasswordResponse.status == HttpStatusCode.OK) {
                        //Update locally
                        personAuthDao.updateAsync(currentPersonAuth!!)
                        view.finish()
                    }else {
                        println("nope")
                    }
                } catch (e: Exception) {
                    print("oops")
                }
            }
        }
    }
}
