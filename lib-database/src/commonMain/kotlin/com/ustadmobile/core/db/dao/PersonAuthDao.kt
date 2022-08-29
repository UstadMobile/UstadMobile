package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.PersonDao.Companion.SESSION_LENGTH
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.door.annotation.Repository.Companion.METHOD_DELEGATE_TO_WEB
import com.ustadmobile.lib.db.entities.AccessToken
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.lib.util.getSystemTimeInMillis

@Dao
abstract class PersonAuthDao : BaseDao<PersonAuth> {


    @Query("SELECT * FROM PersonAuth WHERE personAuthUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long):PersonAuth?

    @Query("SELECT * FROM PersonAuth WHERE personAuthUid = :uid")
    abstract fun findByUid(uid: Long) : PersonAuth?

    @Query("SELECT * FROM Person WHERE username = :username")
    abstract fun findPersonByUsername(username: String): Person?

    @Update
    abstract suspend fun updateAsync(entity: PersonAuth):Int

    @Query("SELECT admin from Person WHERE personUid = :uid")
    abstract fun isPersonAdmin(uid: Long): Boolean

    @Query("UPDATE PersonAuth set passwordHash = :passwordHash " +
            " WHERE personAuthUid = :personUid")
    abstract suspend fun updatePasswordForPersonUid(personUid: Long, passwordHash: String): Int

    @Repository(methodType = METHOD_DELEGATE_TO_WEB)
    open suspend fun resetPassword(personUid: Long, password: String, loggedInPersonUid: Long): Int {
        println("hello")
        val passwordHash = ENCRYPTED_PASS_PREFIX + encryptPassword(password)

        println("Resetting password .. " )

        if (isPersonAdmin(loggedInPersonUid)) {
            println("Password being reset by admin. Allowing.. " )
            //Allow admin to change password of people:
            return changePassword(personUid, passwordHash)
        }else{
            //Allow self to change password:
            if (loggedInPersonUid == personUid) {
                println("Resetting password 2.. " )
                return changePassword(personUid, passwordHash)
            }else{
                println("Unable to reset password cause not the same user.. " )
                return -1
            }
        }
    }

    open suspend fun changePassword(personUid: Long, passwordHash: String):Int {

        val existingPersonAuth = findByUid(personUid)
        if (existingPersonAuth == null) {
            println("PersonAuth doesnt exist for reset password. Creating a new one.." )
            val personAuth = PersonAuth(personUid, passwordHash)
            insert(personAuth)
            println(" .. created new PersonAuth")
        }
        val result = updatePasswordForPersonUid(personUid, passwordHash)
        if (result > 0) {
            println("Update password success")
            return 1
        } else {
            println("Unable to reset password")
            return 0
        }
    }

    @Insert
    abstract fun insertAccessToken(token: AccessToken)

    protected suspend fun onSuccessCreateAccessToken(personUid: Long, username: String): UmAccount {
        val accessToken = AccessToken(personUid,
                getSystemTimeInMillis() + SESSION_LENGTH, getSystemTimeInMillis().toString())

        insertAccessToken(accessToken)
        return (UmAccount(personUid, username, accessToken.token, ""))
    }


    suspend fun authenticate(username: String, password: String) :
            UmAccount? {

        val person = findPersonByUsername(username)
        if(person == null){
            return null
        }

        val loggedInPersonUid = person.personUid

        val personAuth = findByUid(loggedInPersonUid)
        if (personAuth == null) {
            return null
        } else {
            val passwordHash = personAuth.passwordHash

            if (passwordHash!!.startsWith(PLAIN_PASS_PREFIX) &&
                    passwordHash.substring(2) == password) {
                return onSuccessCreateAccessToken(loggedInPersonUid, username)

            } else if (passwordHash.startsWith(ENCRYPTED_PASS_PREFIX) &&
                    authenticateThisEncryptedPassword(password,
                            passwordHash.substring(2))) {
                return onSuccessCreateAccessToken(loggedInPersonUid, username)
            } else if (authenticateThisEncryptedPassword(password,
                            passwordHash)) {
                return onSuccessCreateAccessToken(loggedInPersonUid, username)
            } else {
                return null
            }
        }
    }

    companion object {

        private val KEY_LENGTH = 512

        private val ITERATIONS = 10000

        private val SALT = "fe10fe1010"

        val ENCRYPTED_PASS_PREFIX = "e:"

        val PLAIN_PASS_PREFIX = "p:"

        fun encryptThisPassword(originalPassword: String): String {
            return encryptPassword(originalPassword)
        }

        fun authenticateThisEncryptedPassword(providedPassword: String,
                                              encryptedPassword: String?): Boolean {
            return encryptThisPassword(providedPassword) == encryptedPassword
        }
    }


}
