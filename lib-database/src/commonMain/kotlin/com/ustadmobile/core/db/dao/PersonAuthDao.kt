package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.PersonDao.Companion.SESSION_LENGTH
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.database.annotation.UmRestAccessible
import com.ustadmobile.lib.db.entities.AccessToken
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.lib.util.getSystemTimeInMillis

@UmDao
@Dao
@UmRepository
abstract class PersonAuthDao : BaseDao<PersonAuth> {


    @Query("SELECT * FROM PersonAuth WHERE personAuthUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long):PersonAuth

    @Query("SELECT * FROM PersonAuth WHERE personAuthUid = :uid")
    abstract fun findByUid(uid: Long) : PersonAuth

    @Update
    abstract suspend fun updateAsync(entity: PersonAuth):Int

    @Query("SELECT admin from Person WHERE personUid = :uid")
    abstract fun isPersonAdmin(uid: Long): Boolean

    //TODO: Undo when ready
//    @Query("UPDATE PersonAuth set passwordHash = :passwordHash " +
//            " WHERE personAuthUid = :personUid")
//    abstract fun updatePasswordForPersonUid(personUid: Long, passwordHash: String,
//                                            resultCallback: UmCallback<Int>)
//
//    @UmRestAccessible
//    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
//    fun resetPassword(personUid: Long, password: String,
//                      @UmRestAuthorizedUidParam loggedInPersonUid: Long,
//                      resetCallback: UmCallback<Int>) {
//        val passwordHash = ENCRYPTED_PASS_PREFIX + encryptThisPassword(password)
//
//        if (loggedInPersonUid != personUid) {
//            if (isPersonAdmin(loggedInPersonUid)) {
//                val personAuth = PersonAuth(personUid, passwordHash)
//                val existingPersonAuth = findByUid(personUid)
//                if (existingPersonAuth == null) {
//                    insert(personAuth)
//                }
//                updatePasswordForPersonUid(personUid, passwordHash,
//                        object : UmCallback<Int> {
//                            override fun onSuccess(result: Int?) {
//                                if (result!! > 0) {
//                                    println("Update password success")
//                                    resetCallback.onSuccess(1)
//                                } else {
//                                    resetCallback.onFailure(Exception())
//                                }
//                            }
//
//                            override fun onFailure(exception: Throwable?) {
//                                println("Update password fail")
//                                resetCallback.onFailure(Exception())
//                            }
//                        })
//            } else {
//                println("Update password fail2")
//                resetCallback.onFailure(Exception())
//            }
//        } else {
//            println("Update password fail3")
//            resetCallback.onFailure(Exception())
//        }
//
//    }

    @Insert
    abstract fun insertAccessToken(token: AccessToken)

    protected fun onSuccessCreateAccessToken(personUid: Long, username: String,
                                             callback: UmCallback<UmAccount>) {
        val accessToken = AccessToken(personUid,
                getSystemTimeInMillis() + SESSION_LENGTH)
        insertAccessToken(accessToken)
        callback.onSuccess(UmAccount(personUid, username, accessToken.token, null))
    }

    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    fun authenticate(username: String, loggedInPersonUid: Long, oldPassword: String,
                     resetCallback: UmCallback<UmAccount>) {
        //Authenticate with current password first.
        val personAuthResult = findByUid(loggedInPersonUid)
        if (personAuthResult == null) {
            resetCallback.onFailure(null)
        } else {
            val passwordHash = personAuthResult.passwordHash

            if (passwordHash!!.startsWith(PLAIN_PASS_PREFIX) &&
                    passwordHash.substring(2) == oldPassword) {
                println("ok1")
                onSuccessCreateAccessToken(loggedInPersonUid, username, resetCallback)

            } else if (passwordHash.startsWith(ENCRYPTED_PASS_PREFIX) &&
                    authenticateThisEncryptedPassword(oldPassword,
                            passwordHash.substring(2))) {
                println("ok2")
                onSuccessCreateAccessToken(loggedInPersonUid, username, resetCallback)
            } else if (authenticateThisEncryptedPassword(oldPassword,
                            passwordHash)) {
                println("ok3")
                onSuccessCreateAccessToken(loggedInPersonUid, username, resetCallback)
            } else {
                println("nope1")
                resetCallback.onSuccess(null)
            }
        }

    }

//TODO: Undo when ready
//    @UmRestAccessible
//    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
//    fun selfResetPassword(username: String, oldPassword: String, newPassword: String,
//                          @UmRestAuthorizedUidParam loggedInPersonUid: Long,
//                          resetCallback: UmCallback<Int>) {
//
//        //Generate password Hash
//        val passwordHash = ENCRYPTED_PASS_PREFIX + encryptThisPassword(newPassword)
//
//        authenticate(username, loggedInPersonUid, oldPassword, object : UmCallback<UmAccount> {
//            override fun onSuccess(result: UmAccount?) {
//                if (result == null) {
//                    resetCallback.onFailure(Exception())
//                } else {
//                    //Create new person auth entry if it doesnt exist
//                    val existingPersonAuth = findByUid(loggedInPersonUid)
//                    if (existingPersonAuth == null) {
//                        val personAuth = PersonAuth(loggedInPersonUid, passwordHash)
//                        insert(personAuth)
//                    }
//
//                    //Update password for Person
//                    updatePasswordForPersonUid(loggedInPersonUid, passwordHash,
//                            object : UmCallback<Int> {
//                                override fun onSuccess(result: Int?) {
//                                    if (result!! > 0) {
//                                        println("Update password success")
//                                        resetCallback.onSuccess(1)
//                                    } else {
//                                        resetCallback.onFailure(Exception())
//                                    }
//                                }
//
//                                override fun onFailure(exception: Throwable?) {
//                                    println("Update password fail")
//                                    resetCallback.onFailure(Exception())
//                                }
//                            })
//                }
//
//
//            }
//
//            override fun onFailure(exception: Throwable?) {
//                resetCallback.onFailure(Exception())
//            }
//        })
//
//    }

    companion object {

        private val KEY_LENGTH = 512

        private val ITERATIONS = 10000

        private val SALT = "fe10fe1010"

        val ENCRYPTED_PASS_PREFIX = "e:"

        val PLAIN_PASS_PREFIX = "p:"

        fun encryptThisPassword(originalPassword: String): String {
            return encryptPassword(originalPassword)

            //Update: Done. Part of Encrypt.kt thingi
            //TODOne: Implement/Fix this.
            /*
            val keySpec = PBEKeySpec(originalPassword.toCharArray(), SALT.toByteArray(),
                    ITERATIONS, KEY_LENGTH)
            try {
                val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                return String(Base64Coder.encode(keyFactory.generateSecret(keySpec).getEncoded()))
            } catch (e: NoSuchAlgorithmException) {
                //should not happen
                throw AssertionError("Error hashing password" + e.message, e)
            } catch (e: InvalidKeySpecException) {
                throw AssertionError("Error hashing password" + e.message, e)
            }
            */
        }

        fun authenticateThisEncryptedPassword(providedPassword: String,
                                              encryptedPassword: String?): Boolean {
            return encryptThisPassword(providedPassword) == encryptedPassword
        }
    }


}
