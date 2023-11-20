package com.ustadmobile.core.account

import org.junit.Assert
import org.junit.Test
import java.util.Arrays

class StringEncryptTest {

    @Test
    fun givenSameParams_whenEncrypted_thenShoudlMatch() {
        val secret = "timeToCheck"
        val salt = "fefe1010fe"
        val iterations = 10000
        val keylen = 512
        val e1 = secret.encryptWithPbkdf2V2(salt, iterations, keylen)
        val e2 = secret.encryptWithPbkdf2V2(salt, iterations, keylen)
        Assert.assertArrayEquals(e1, e2)
    }

    @Test
    fun givenSameParams_differentSecret_thenShouldNotMatch() {
        val secret = "timeToCheck"
        val salt = "fefe1010fe"
        val iterations = 10000
        val keylen = 512
        val e1 = secret.encryptWithPbkdf2V2(salt, iterations, keylen)
        val e2 = "other".encryptWithPbkdf2V2(salt, iterations, keylen)
        Assert.assertFalse(Arrays.equals(e1, e2))
    }


}