package com.ustadmobile.lib.util

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

private const val KEY_LENGTH = 512

private const val ITERATIONS = 10000

private const val SALT = "fe10fe1010"

val ENCRYPTED_PASS_PREFIX = "e:"

val PLAIN_PASS_PREFIX = "p:"

actual fun authenticateEncryptedPassword(providedPassword: String, encryptedPassword: String): Boolean {
    return encryptPassword(providedPassword) == encryptedPassword
}

actual fun encryptPassword(originalPassword: String): String {
    val keySpec = PBEKeySpec(originalPassword.toCharArray(), SALT.toByteArray(),
            ITERATIONS, KEY_LENGTH)
    try {
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return Base64.getEncoder().encodeToString(keyFactory.generateSecret(keySpec).getEncoded())
    } catch (e: NoSuchAlgorithmException) {
        //should not happen
        throw AssertionError("Error hashing password" + e.message, e)
    } catch (e: InvalidKeySpecException) {
        throw AssertionError("Error hashing password" + e.message, e)
    }
}