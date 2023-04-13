package com.ustadmobile.lib.rest

import com.ustadmobile.core.util.ext.encodeBase64
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Server-side Pbkdf2 encryption for Javascript usage. The Javascript already sends the
 * password to server over an encrypted connection, so this is not a security issue.
 *
 * It should eventually be replaced with use of the web crypto API that is available
 * on modern browsers.
 *
 * Implemented to serve com.ustadmobile.core.util.ext.StringEncryptExt on Javascript
 */
fun Route.Pbkdf2Route() {
    get("encrypt") {
        val secret = call.request.queryParameters["secret"]
        val salt = call.request.queryParameters["salt"]
        val iterations = call.request.queryParameters["iterations"]?.toInt() ?: 0
        val keyLength = call.request.queryParameters["keyLength"]?.toInt() ?: 0

        if(secret != null && salt != null && iterations > 0 && keyLength > 0) {
            val keySpec = PBEKeySpec(secret.toCharArray(), salt.toByteArray(),
                iterations, keyLength)

            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val buffer = keyFactory.generateSecret(keySpec).encoded
            call.respondText(contentType = ContentType.Text.Plain) {
                buffer.encodeBase64()
            }
        }else {
            call.respondText(status = HttpStatusCode.BadRequest) {
                "Bad Request"
            }
        }
    }
}
