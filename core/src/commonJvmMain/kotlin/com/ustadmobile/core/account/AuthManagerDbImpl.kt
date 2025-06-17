package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.core.util.ext.isDateOfBirthAMinor
import com.ustadmobile.lib.db.entities.PersonAuth2
import com.ustadmobile.lib.db.entities.PersonParentJoin.Companion.STATUS_APPROVED
import kotlinx.datetime.Instant
import nl.adaptivity.xmlutil.serialization.writeAsXML
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import java.security.KeyPairGenerator
import java.security.KeyStore

class AuthManagerDbImpl(
    internal val learningSpace: LearningSpace,
    override val di: DI,
    private val keyPairGenerator: KeyPairGenerator,
)  : DIAware {

    private val dataLayer: UmAppDataLayer by on(learningSpace).instance()

    suspend fun authenticate(
        username: String,
        password: String
    ): AuthResult {
        val passwordDoubleHashed = ByteArray(1)//doublePbkdf2Hash(password)
        val personAuth2 = dataLayer.repositoryOrLocalDb.personAuth2Dao().findByUsername(username)
        val authMatch = personAuth2?.pauthAuth?.base64StringToByteArray()
            .contentEquals(passwordDoubleHashed)

        val authorizedPerson = if(authMatch) {
            dataLayer.localDb.personDao().findByUidAsync(personAuth2?.pauthUid ?: 0L)
        }else {
            null
        }

        //Check if this is an account for a minor which requires parental consent
        if(authorizedPerson != null &&
            Instant.fromEpochMilliseconds(authorizedPerson.dateOfBirth).isDateOfBirthAMinor()
        ) {
            val parentJoins = dataLayer.localDb.personParentJoinDao().findByMinorPersonUid(authorizedPerson.personUid)

            if(!parentJoins.any { it.ppjStatus == STATUS_APPROVED }) {
                return AuthResult(null, false,
                    AuthResult.REASON_NEEDS_CONSENT)
            }
        }

        return AuthResult(authorizedPerson, authorizedPerson != null)
    }

    suspend fun setAuth(personUid: Long, password: String) {
        //val encryptedPass = doublePbkdf2HashAsBase64(password)
        //TODO


        dataLayer.repositoryOrLocalDb.personAuth2Dao().insertAsync(PersonAuth2().apply {
            pauthUid = personUid
            pauthMechanism = PersonAuth2.AUTH_MECH_PBKDF2_DOUBLE
            pauthAuth = "f"
        })
    }
}