package com.ustadmobile.core.account

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.db.dao.PersonAuthDaoCommon
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.schedule.age
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.core.util.ext.doublePbkdf2Hash
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.core.util.ext.insertPersonAuthCredentials2
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.PersonAuth2
import com.ustadmobile.lib.db.entities.PersonParentJoin.Companion.STATUS_APPROVED
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.authenticateEncryptedPassword
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

/**
 * AuthManager is a simple clearing house for authenticating users. This can support rate limiting
 * etc. It caches the authentication salt and gets any needed password hashing params from the DI.
 */
class AuthManager(
    endpoint: Endpoint,
    override val di: DI
) : DIAware {

    private val repo: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val pbkdf2Params: Pbkdf2Params by instance()

    private var site: Site? = null

    private suspend fun getSite(): Site {
        site?.also {
            return it
        }

        val siteVal = repo.siteDao.getSiteAsync() ?: throw IllegalStateException("No site!")
        site = siteVal
        return siteVal
    }

    suspend fun authenticate(
        username: String,
        password: String,
        fallbackToOldPersonAuth: Boolean = false
    ): AuthResult {

        val site: Site = repo.siteDao.getSiteAsync() ?: throw IllegalStateException("No site!")
        val authSalt = site.authSalt ?: throw IllegalStateException("No auth salt!")

        val passwordDoubleHashed = password.doublePbkdf2Hash(authSalt, pbkdf2Params)
        val personAuth2 = repo.personAuth2Dao.findByUsername(username)
        val authMatch = personAuth2?.pauthAuth?.base64StringToByteArray()
            .contentEquals(passwordDoubleHashed)


        var authorizedPerson = if(authMatch) {
            repo.personDao.findByUidAsync(personAuth2?.pauthUid ?: 0L)
        }else {
            null
        }

        if(authorizedPerson == null && fallbackToOldPersonAuth) {
            val person = db.personDao.findUidAndPasswordHashAsync(username)
            if(person != null
                && ((person.passwordHash?.startsWith(PersonAuthDaoCommon.PLAIN_PASS_PREFIX) == true
                        && person.passwordHash?.substring(2) == password)
                        ||(person.passwordHash?.startsWith(PersonAuthDaoCommon.ENCRYPTED_PASS_PREFIX) == true &&
                        authenticateEncryptedPassword(password, person.passwordHash?.substring(2) ?: "")))) {
                authorizedPerson = db.personDao.findByUidAsync(person.personUid)

                //Create the auth object
                repo.personAuth2Dao.insertAsync(PersonAuth2().apply {
                    pauthUid = person.personUid
                    pauthMechanism = PersonAuth2.AUTH_MECH_PBKDF2_DOUBLE
                    pauthAuth = password.doublePbkdf2Hash(authSalt, pbkdf2Params).encodeBase64()
                })
            }
        }

        //Check if this is an account for a minor which requires parental consent
        if(authorizedPerson != null &&
            DateTime(authorizedPerson.dateOfBirth).age()  < UstadMobileConstants.MINOR_AGE_THRESHOLD) {
            val parentJoins = db.personParentJoinDao.findByMinorPersonUid(authorizedPerson.personUid)

            if(!parentJoins.any { it.ppjStatus == STATUS_APPROVED }) {
                return AuthResult(null, false,
                    AuthResult.REASON_NEEDS_CONSENT)
            }
        }


        return AuthResult(authorizedPerson, authorizedPerson != null)
    }

    suspend fun setAuth(personUid: Long, password: String) {
        repo.insertPersonAuthCredentials2(personUid, password, pbkdf2Params, getSite())
    }


}