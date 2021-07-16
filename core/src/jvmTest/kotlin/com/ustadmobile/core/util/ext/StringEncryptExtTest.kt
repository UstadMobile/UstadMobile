package com.ustadmobile.core.util.ext

import kotlin.test.Test
import kotlin.test.assertEquals

class StringEncryptExtTest {

    @Test
    fun givenPlanPassword_whenEncryptingWithPbkdf2_shouldBeEncrypted(){
        val expectedFromJs = "7fc4JUghxV2mHmr6IO/QxlfLlBw="
        val encryptedPassword = "password".encryptWithPbkdf2("salt",5000,20)
        assertEquals("Encrypted password is the same as in JS",expectedFromJs, encryptedPassword.encodeBase64())
    }
}