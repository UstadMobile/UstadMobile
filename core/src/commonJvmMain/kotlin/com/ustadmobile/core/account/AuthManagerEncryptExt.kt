package com.ustadmobile.core.account

import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import io.ktor.util.encodeBase64

internal actual suspend fun AuthManager.doublePbkdf2Hash(password: String): ByteArray {
    val db: UmAppDatabase = di.on(learningSpace).direct.instance(tag = DoorTag.TAG_DB)
    val salt = db.siteDao().getSiteAuthSaltAsync()
        ?: throw IllegalStateException("No auth salt!")
    val pbkdf2Params: Pbkdf2Params = di.on(learningSpace).direct.instance()

    return password.doubleEncryptWithPbkdf2V2(salt, pbkdf2Params.iterations, pbkdf2Params.keyLength)
}

internal actual suspend fun AuthManager.doublePbkdf2HashAsBase64(password: String): String {
    return doublePbkdf2Hash(password).encodeBase64()
}

internal actual suspend fun AuthManager.encryptPbkdf2(password: String): ByteArray {
    val db: UmAppDatabase = di.on(learningSpace).direct.instance(tag = DoorTag.TAG_DB)
    val salt = db.siteDao().getSiteAuthSaltAsync()
        ?: throw IllegalStateException("No auth salt!")
    val pbkdf2Params: Pbkdf2Params = di.on(learningSpace).direct.instance()

    return password.encryptWithPbkdf2V2(salt, pbkdf2Params.iterations, pbkdf2Params.keyLength)
}

