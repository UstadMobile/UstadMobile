package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.PersonAuth2
import com.ustadmobile.lib.db.entities.PersonParentJoin.Companion.STATUS_APPROVED
import kotlinx.datetime.Instant
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.on


/**
 * AuthManager is a simple clearing house for authenticating users. This can support rate limiting
 * etc.
 *
 * Passwords are stored in the database with two rounds of PBKDF2 encryption - first encrypt the
 * password with PBKDF2, convert that to a hex string, then encrypt that with PBKDF2 again. Passwords
 * are salted with the randomly generated salt string that is on the Site entity. PBKDF2 params are
 * preset and stored in the DI manager.
 *
 * This way the user session auth string can be checked against the stored password, without
 * actually containing the password itself. User sessions can be started offline.
 *
 * ALL auth related requests run through this manager.
 */
class AuthManager(
    internal val endpoint: Endpoint,
    override val di: DI
) : DIAware {

    private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val repo: UmAppDatabase? by on(endpoint).instanceOrNull(tag = DoorTag.TAG_REPO)

    suspend fun authenticate(
        username: String,
        password: String
    ): AuthResult {
        val passwordDoubleHashed = doublePbkdf2Hash(password)
        val personAuth2 = (repo ?: db).personAuth2Dao().findByUsername(username)
        val authMatch = personAuth2?.pauthAuth?.base64StringToByteArray()
            .contentEquals(passwordDoubleHashed)

        val authorizedPerson = if(authMatch) {
            db.personDao().findByUidAsync(personAuth2?.pauthUid ?: 0L)
        }else {
            null
        }

        //Check if this is an account for a minor which requires parental consent
        if(authorizedPerson != null &&
            Instant.fromEpochMilliseconds(authorizedPerson.dateOfBirth).isDateOfBirthAMinor()
        ) {
            val parentJoins = db.personParentJoinDao().findByMinorPersonUid(authorizedPerson.personUid)

            if(!parentJoins.any { it.ppjStatus == STATUS_APPROVED }) {
                return AuthResult(null, false,
                    AuthResult.REASON_NEEDS_CONSENT)
            }
        }

        return AuthResult(authorizedPerson, authorizedPerson != null)
    }

    suspend fun setAuth(personUid: Long, password: String) {
        val encryptedPass = doublePbkdf2HashAsBase64(password)
        (repo ?: db).personAuth2Dao().insertAsync(PersonAuth2().apply {
            pauthUid = personUid
            pauthMechanism = PersonAuth2.AUTH_MECH_PBKDF2_DOUBLE
            pauthAuth = encryptedPass
        })
    }


}