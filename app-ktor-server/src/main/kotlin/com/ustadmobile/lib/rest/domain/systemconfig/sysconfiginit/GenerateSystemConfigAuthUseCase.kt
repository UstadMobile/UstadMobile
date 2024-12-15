package com.ustadmobile.lib.rest.domain.systemconfig.sysconfiginit

import com.ustadmobile.centralappconfigdb.db.SystemConfigAuth
import com.ustadmobile.centralappconfigdb.sqlite.CentralAppConfigDb
import com.ustadmobile.core.domain.pbkdf2.Pbkdf2EncryptUseCase
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.lib.util.randomString
import java.io.File

class GenerateSystemConfigAuthUseCase(
    private val encryptor: Pbkdf2EncryptUseCase,
    private val dataDirPath: File,
) {
    fun invoke(centralAppConfigDb: CentralAppConfigDb) {
        val salt = randomString(16)
        val password = randomString(16)
        val encryptedPassBase64 = encryptor(password, salt).encodeBase64()

        centralAppConfigDb.systemConfigAuthQueries.insert(
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