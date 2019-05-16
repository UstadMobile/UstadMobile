package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.PersonAuth

@UmDao
@UmRepository
abstract class PersonAuthDao : BaseDao<PersonAuth> {
    companion object {

        private const val KEY_LENGTH = 512

        private const val ITERATIONS = 10000

        private const val SALT = "fe10fe1010"

        const val ENCRYPTED_PASS_PREFIX = "e:"

        const val PLAIN_PASS_PREFIX = "p:"

        // TODO
        /*fun encryptPassword(originalPassword: String): String {
            val keySpec = PBEKeySpec(originalPassword.toCharArray(), SALT.toByteArray(),
                    ITERATIONS, KEY_LENGTH)
            try {
                val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                return Base64Coder.encodeToString(keyFactory.generateSecret(keySpec).getEncoded())
            } catch (e: NoSuchAlgorithmException) {
                //should not happen
                throw AssertionError("Error hashing password" + e.message, e)
            } catch (e: InvalidKeySpecException) {
                throw AssertionError("Error hashing password" + e.message, e)
            }

        } */

        /*fun authenticateEncryptedPassword(providedPassword: String,
                                          encryptedPassword: String): Boolean {
            return encryptPassword(providedPassword) == encryptedPassword
        } */
    }


}
