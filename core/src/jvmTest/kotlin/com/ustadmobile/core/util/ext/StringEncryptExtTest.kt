package com.ustadmobile.core.util.ext

import kotlin.test.Test
import kotlin.test.assertEquals

class StringEncryptExtTest {

    @Test
    fun givenPlanPassword_whenEncryptingWithPbkdf2_shouldBeEncrypted(){
        val expectedFromJs = "boi+i61+rp0="
        val encryptedPassword = "password".encryptWithPbkdf2("salt",1000,64)
        assertEquals(expectedFromJs, encryptedPassword.encodeBase64(), "Encrypted password is the same as in JS")
    }
}