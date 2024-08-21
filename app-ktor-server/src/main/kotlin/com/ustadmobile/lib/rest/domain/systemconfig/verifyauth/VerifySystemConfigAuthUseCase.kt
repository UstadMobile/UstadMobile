package com.ustadmobile.lib.rest.domain.systemconfig.verifyauth

import com.ustadmobile.appconfigdb.SystemDb
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.pbkdf2.Pbkdf2AuthenticateUseCase
import com.ustadmobile.core.util.ext.base64StringToByteArray
import io.ktor.server.application.ApplicationCall
import io.ktor.util.decodeBase64Bytes

/**
 * Use case to verify that a request is authorized to use the system config API
 */
class VerifySystemConfigAuthUseCase(
    private val systemDb: SystemDb,
    private val pbkdf2AuthenticateUseCase: Pbkdf2AuthenticateUseCase,
) {

    suspend operator fun invoke(call: ApplicationCall) {
        val authHeader = call.request.headers["Authorization"] ?: throw HttpApiException(401,
            "Missing Authorization header")

        val (authScheme, authData) = Regex("\\s+").split(authHeader, limit = 2)

        if(!authScheme.equals("Basic", true))
            throw HttpApiException(400, "Only basic auth supported")

        val authUser = authData.decodeBase64Bytes().decodeToString()
        val (username, password) = authUser.split(":")

        val sysConfigAuth = systemDb.systemConfigAuthDao().findById(username)
            ?: throw HttpApiException(401, "Invalid username")

        if(
            !pbkdf2AuthenticateUseCase(
                password = password,
                encryptedPassword = sysConfigAuth.scaAuthCredential.base64StringToByteArray(),
                salt = sysConfigAuth.scaAuthSalt ?: throw IllegalStateException("No salt"),
            )
        ) {
            throw HttpApiException(401, "Invalid password")
        }
    }

}