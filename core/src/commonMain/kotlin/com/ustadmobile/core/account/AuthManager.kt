package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.util.ext.encryptWithPbkdf2
import com.ustadmobile.core.util.ext.insertPersonAuthCredentials2
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.PersonAuth2
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

        val passwordDoubleHashed = password.encryptWithPbkdf2(authSalt, pbkdf2Params)
            .encryptWithPbkdf2(authSalt, pbkdf2Params)

        var authorizedPerson = repo.personDao.findByUsernameAndPasswordHash2(username,
            passwordDoubleHashed)

        if(authorizedPerson == null && fallbackToOldPersonAuth) {
            val person = db.personDao.findUidAndPasswordHashAsync(username)
            if(person != null
                && ((person.passwordHash.startsWith(PersonAuthDao.PLAIN_PASS_PREFIX)
                        && person.passwordHash.substring(2) == password)
                        ||(person.passwordHash.startsWith(PersonAuthDao.ENCRYPTED_PASS_PREFIX) &&
                        authenticateEncryptedPassword(password, person.passwordHash.substring(2))))) {
                authorizedPerson = db.personDao.findByUid(0L)

                //Create the auth object
                repo.personAuth2Dao.insertAsync(PersonAuth2().apply {
                    pauthUid = person.personUid
                    pauthMechanism = PersonAuth2.AUTH_MECH_PBKDF2_DOUBLE
                    pauthAuth = password.encryptWithPbkdf2(authSalt, pbkdf2Params)
                        .encryptWithPbkdf2(authSalt, pbkdf2Params)
                })
            }
        }

        return AuthResult(authorizedPerson, authorizedPerson != null)
    }

    suspend fun setAuth(personUid: Long, password: String) {
        repo.insertPersonAuthCredentials2(personUid, password, pbkdf2Params, getSite())
    }


}