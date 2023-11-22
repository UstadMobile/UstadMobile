package com.ustadmobile.core.account

import com.ustadmobile.core.util.ext.encodeBase64
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on

/**
 * Server-side Pbkdf2 encryption for Javascript usage. This is used by the AuthManager on
 * Javascript. Doing the actual encryption on the client side using Javascript is possible in
 * theory, but in practice performance was extremely challenging and there is no need to run this
 * on the client side at the moment.
 */
fun Route.Pbkdf2Route() {
    get("encryptPbkdf2") {
        val secret = call.request.queryParameters["secret"]!!
        val di: DI = closestDI()
        val authManager: AuthManager = di.direct.on(call).instance()
        val encrypted = authManager.encryptPbkdf2(secret)
        call.respondText(contentType = io.ktor.http.ContentType.Text.Plain) {
            encrypted.encodeBase64()
        }
    }

    get("doubleEncryptPbkdf2") {
        val di: DI = closestDI()
        val secret = call.request.queryParameters["secret"]!!
        val authManager: AuthManager = di.on(call).direct.instance()
        val doubleEncrypted = authManager.doublePbkdf2Hash(secret)
        call.respondText(contentType = io.ktor.http.ContentType.Text.Plain) {
            doubleEncrypted.encodeBase64()
        }
    }

}


