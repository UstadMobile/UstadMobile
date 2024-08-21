package com.ustadmobile.lib.rest.domain.systemconfig.sysconfiginit

import com.ustadmobile.appconfigdb.entities.SystemConfigAuth
import com.ustadmobile.core.domain.pbkdf2.Pbkdf2EncryptUseCase
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.DoorDatabaseCallbackSync
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.lib.util.randomString
import java.io.File

class GenerateSystemConfigAuthCallback(
    private val encryptor: Pbkdf2EncryptUseCase,
    private val dataDirPath: File,
): DoorDatabaseCallbackSync {
    override fun onCreate(db: DoorSqlDatabase) {
        val salt = randomString(16)
        val password = randomString(16)
        val encryptedPassBase64 = encryptor(password, salt).encodeBase64()

        db.execSQL("""
            INSERT INTO SystemConfigAuth(scaAuthType, scaAuthId, scaAuthCredential, scaAuthSalt) 
            VALUES(${SystemConfigAuth.TYPE_PASSWORD}, 'admin', '$encryptedPassBase64', '$salt')
        """)

        File(dataDirPath, "admin.txt").writeText(password)
    }

    override fun onOpen(db: DoorSqlDatabase) {
        //Do nothing
    }
}