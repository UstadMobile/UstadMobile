package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import java.io.File
import java.net.URI

interface ContainerImporter {

    suspend fun importContentEntryFromFile(contentEntryUid: Long, mimeType: String?, containerBaseDir: String,
                                           file: File, db: UmAppDatabase, dbRepo: UmAppDatabase, importMode: Int, context: Any): Container
}