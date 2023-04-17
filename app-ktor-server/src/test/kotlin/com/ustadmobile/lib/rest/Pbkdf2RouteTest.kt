package com.ustadmobile.lib.rest

import com.ustadmobile.core.util.ext.base64StringToByteArray
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.gson.*
import io.ktor.server.config.*
import io.ktor.server.routing.routing
import io.ktor.server.testing.*
import org.junit.Assert
import org.junit.Test
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class Pbkdf2RouteTest {

    @Test
    fun givenSecretAndParams_whenGetEncryptCalled_thenShouldEncryptAndMatchJvmResult() {
        val secret = "password"
        val salt = "pinksalt"
        val iterations = 10000
        val keyLength = 512

        testApplication {
            environment {
                config = MapApplicationConfig("ktor.environment" to "test")
            }

            val client = createClient {
                install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                    gson()
                }
            }

            application {
                routing {
                    Pbkdf2Route()
                }
            }

            val response = client.get("/encrypt") {
                parameter("secret", secret)
                parameter("salt", salt)
                parameter("iterations", iterations.toString())
                parameter("keyLength", keyLength.toString())
            }.bodyAsText()

            val digestedByteArr = response.base64StringToByteArray()

            val keySpec = PBEKeySpec(secret.toCharArray(), salt.toByteArray(),
                iterations, keyLength)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val buffer = keyFactory.generateSecret(keySpec).encoded

            Assert.assertArrayEquals("Response matches", buffer, digestedByteArr)
        }
    }
}