package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.defaultJsonSerializer
import kotlin.test.*

class StringEncryptExtTest {
    @BeforeTest
    fun init(){
        defaultJsonSerializer()
    }

    @Suppress("UNUSED_VARIABLE") // secret is used by js code
    @Test
    fun givenPlanPassword_whenEncryptingWithPbkdf2_shouldBeEncrypted(){
        val expectedFromJvm = "boi+i61+rp0="
        val secret = "password".encryptWithPbkdf2("salt",1000,64)
        val encryptedPassword = js("secret.toString('base64')").toString()
        assertEquals(encryptedPassword, expectedFromJvm, "Encrypted password is the same as on JVM")
    }
}