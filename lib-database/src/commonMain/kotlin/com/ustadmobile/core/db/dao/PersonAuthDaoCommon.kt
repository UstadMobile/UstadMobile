package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.util.encryptPassword

object PersonAuthDaoCommon {

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