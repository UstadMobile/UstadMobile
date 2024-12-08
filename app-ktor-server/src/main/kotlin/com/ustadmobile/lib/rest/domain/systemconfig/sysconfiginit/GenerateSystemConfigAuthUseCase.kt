package com.ustadmobile.lib.rest.domain.systemconfig.sysconfiginit

import com.ustadmobile.core.domain.pbkdf2.Pbkdf2EncryptUseCase
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.systemdb.sqlite.SystemDb
import systemdb.data.SystemConfigAuth
import java.io.File

class GenerateSystemConfigAuthUseCase(
    private val encryptor: Pbkdf2EncryptUseCase,
    private val dataDirPath: File,
) {
    fun invoke(systemDb: SystemDb) {
        val salt = randomString(16)
        val password = randomString(16)
        val encryptedPassBase64 = encryptor(password, salt).encodeBase64()

        systemDb.systemConfigAuthQueries.insert(
            SystemConfigAuth(
                scaUid = 1,
                scaAuthType = 1,
                scaAuthId = "admin",
                scaAuthCredential = encryptedPassBase64,
                scaAuthSalt = salt
            )
        )

        File(dataDirPath, "admin.txt").writeText(password)
    }

}