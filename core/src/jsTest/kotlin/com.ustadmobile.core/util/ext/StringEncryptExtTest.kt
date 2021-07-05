package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.defaultJsonSerializer
import kotlin.test.*

class StringEncryptExtTest {
    @BeforeTest
    fun init(){
        defaultJsonSerializer()
    }

    @Test
    fun givenPlanPassword_whenEncryptingWithPbkdf2_shouldBeEncrypted(){
        val expectedFromJvm = "7fc4JUghxV2mHmr6IO/QxlfLlBw="
        val encryptedPassword = "password".encryptWithPbkdf2("salt",5000,20)
        assertEquals("Encrypted password is the same as on JVM",expectedFromJvm, encryptedPassword)
    }
}